/*
 * IBM Confidential
 * PID 5724-K74
 *
 * . 2023
 */

/*
 * This source file is used to unzip a specified file to the specified
 * directory.  We need to use the zlib library instead of the standard
 * windows unzip library due to serious performance issues with the 
 * standard Windows unzip library.
 *
 * The origin of this program stems from the miniunz.c source file, which
 * is a part of the 'contrib' directory of the zlib source.  The source code
 * has been tidied up and trimmed down to the bare necessities.
 */

#if (!defined(_WIN32)) && (!defined(WIN32)) && (!defined(__APPLE__))
        #ifndef __USE_FILE_OFFSET64
                #define __USE_FILE_OFFSET64
        #endif
        #ifndef __USE_LARGEFILE64
                #define __USE_LARGEFILE64
        #endif
        #ifndef _LARGEFILE64_SOURCE
                #define _LARGEFILE64_SOURCE
        #endif
        #ifndef _FILE_OFFSET_BIT
                #define _FILE_OFFSET_BIT 64
        #endif
#endif

#if defined(__APPLE__) || defined(__HAIKU__) || defined(MINIZIP_FOPEN_NO_64)
// In darwin and perhaps other BSD variants off_t is a 64 bit value, hence no need for specific 64 bit functions
#define FOPEN_FUNC(filename, mode) fopen(filename, mode)
#define FTELLO_FUNC(stream) ftello(stream)
#define FSEEKO_FUNC(stream, offset, origin) fseeko(stream, offset, origin)
#else
#define FOPEN_FUNC(filename, mode) fopen64(filename, mode)
#define FTELLO_FUNC(stream) ftello64(stream)
#define FSEEKO_FUNC(stream, offset, origin) fseeko64(stream, offset, origin)
#endif


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/stat.h>

#ifdef _WIN32
# include <direct.h>
# include <io.h>
#else
# include <unistd.h>
# include <utime.h>
#endif


#include "unzip.h"

#define CASESENSITIVITY (0)
#define WRITEBUFFERSIZE (8192)
#define MAXFILENAME (256)

#ifdef _WIN32
#define USEWIN32IOAPI
#include "iowin32.h"
#endif

/*
  mini unzip, demo of unzip package

  usage :
  Usage : ibm_vdi_unzip file.zip extract.dir
*/


static void change_file_date(const char *filename, uLong dosdate, 
                                tm_unz tmu_date) {
#ifdef _WIN32
    HANDLE hFile;
    FILETIME ftm,ftLocal,ftCreate,ftLastAcc,ftLastWrite;

    hFile = CreateFileA(filename,GENERIC_READ | GENERIC_WRITE,
                      0,NULL,OPEN_EXISTING,0,NULL);

    GetFileTime(hFile,&ftCreate,&ftLastAcc,&ftLastWrite);
    DosDateTimeToFileTime((WORD)(dosdate>>16),(WORD)dosdate,&ftLocal);
    LocalFileTimeToFileTime(&ftLocal,&ftm);
    SetFileTime(hFile,&ftm,&ftLastAcc,&ftm);
    CloseHandle(hFile);
#else
#if defined(unix) || defined(__APPLE__)
    (void)dosdate;
    struct utimbuf ut;
    struct tm newdate;
    newdate.tm_sec = tmu_date.tm_sec;
    newdate.tm_min=tmu_date.tm_min;
    newdate.tm_hour=tmu_date.tm_hour;
    newdate.tm_mday=tmu_date.tm_mday;
    newdate.tm_mon=tmu_date.tm_mon;
    if (tmu_date.tm_year > 1900)
        newdate.tm_year=tmu_date.tm_year - 1900;
    else
        newdate.tm_year=tmu_date.tm_year ;
    newdate.tm_isdst=-1;

    ut.actime=ut.modtime=mktime(&newdate);
    utime(filename,&ut);
#else
    (void)filename;
    (void)dosdate;
    (void)tmu_date;
#endif
#endif
}


static int mymkdir(const char* dirname) {
    int ret=0;

#ifdef _WIN32
    ret = _mkdir(dirname);
#elif unix
    ret = mkdir (dirname,0775);
#elif __APPLE__
    ret = mkdir (dirname,0775);
#else
    (void)dirname;
#endif

    return ret;
}

static int makedir(const char *newdir) 
{
    char*  buffer = 0x00;
    char*  p      = 0x00;
    size_t len    = strlen(newdir);

    if (len == 0) {
        return 0;
    }

    buffer = (char*)malloc(len+1);
    if (buffer == NULL) {
        printf("Error allocating memory\n");
        return UNZ_INTERNALERROR;
    }

    strcpy(buffer,newdir);

    if (buffer[len-1] == '/') {
        buffer[len-1] = '\0';
    }

    if (mymkdir(buffer) == 0) {
        free(buffer);
        return 1;
    }

    p = buffer+1;

    while (1) {
        char hold;

        while(*p && *p != '\\' && *p != '/') {
            p++;
        }

        hold = *p;
        *p   = 0;

        if ((mymkdir(buffer) == -1) && (errno == ENOENT)) {
            printf("couldn't create directory %s\n",buffer);
            free(buffer);
            return 0;
        }
        if (hold == 0) {
            break;
        }
        *p++ = hold;
    }

    free(buffer);
    return 1;
}

static int do_extract_currentfile(unzFile uf) 
{
    char  filename_inzip[256];
    char* filename_withoutpath;
    char* p;
    int   err = UNZ_OK;
    FILE* fout = NULL;
    void* buf;
    uInt  size_buf;

    unz_file_info64 file_info;

    err = unzGetCurrentFileInfo64(uf, &file_info, filename_inzip, 
                                    sizeof(filename_inzip), NULL, 0, NULL, 0);

    if (err != UNZ_OK) {
        printf("error %d with zipfile in unzGetCurrentFileInfo\n", err);
        return err;
    }

    size_buf = WRITEBUFFERSIZE;
    buf      = (void*)malloc(size_buf);

    if (buf == NULL) {
        printf("Error allocating memory\n");
        return UNZ_INTERNALERROR;
    }

    p = filename_withoutpath = filename_inzip;

    while ((*p) != '\0') {

        if (((*p) == '/') || ((*p) == '\\')) {
            filename_withoutpath = p+1;
        }
        p++;
    }

    if ((*filename_withoutpath) == '\0') {
        mymkdir(filename_inzip);
    } else {
        const char* write_filename;
        int         skip = 0;

        write_filename = filename_inzip;

        err = unzOpenCurrentFile(uf);

        if (err != UNZ_OK) {
            printf("error %d with zipfile in unzOpenCurrentFilePassword\n",err);
            return err;
        }

        if ((skip == 0) && (err == UNZ_OK)) {
            fout=FOPEN_FUNC(write_filename,"wb");

            /* some zipfile don't contain directory alone before file */
            if (fout == NULL && filename_withoutpath != (char*)filename_inzip) {
                char c = *(filename_withoutpath-1);

                *(filename_withoutpath-1) = '\0';
                makedir(write_filename);

                *(filename_withoutpath-1) = c;
                fout=FOPEN_FUNC(write_filename,"wb");
            }

            if (fout==NULL) {
                printf("error opening %s\n",write_filename);

                free(buf);

                return 1;
            }
        }

        if (fout != NULL) {
            do {
                err = unzReadCurrentFile(uf,buf,size_buf);
                if (err < 0) {
                    printf("error %d with zipfile in unzReadCurrentFile\n",err);
                    break;
                }
                if (err > 0) {
                    if (fwrite(buf, (unsigned)err, 1, fout) !=1 ) {
                        printf("error in writing extracted file\n");
                        err = UNZ_ERRNO;
                        break;
                    }
                }
            } while (err > 0);

            if (fout) {
                fclose(fout);
            }

            if (err==0) {
                change_file_date(write_filename, file_info.dosDate,
                                 file_info.tmu_date);
            }
        }

        if (err == UNZ_OK) {
            err = unzCloseCurrentFile (uf);
            if (err != UNZ_OK) {
                printf("error %d with zipfile in unzCloseCurrentFile\n",err);

                free(buf);

                return err;
            }
        } else {
            unzCloseCurrentFile(uf); /* don't lose the error */
        }
    }

    free(buf);

    return err;
}


static int do_extract(unzFile uf) 
{
    uLong             i;
    unz_global_info64 gi;
    int               err;

    err = unzGetGlobalInfo64(uf, &gi);

    if (err != UNZ_OK) {
        printf("error %d with zipfile in unzGetGlobalInfo \n", err);

        return 1;
    }

    for (i=0; i<gi.number_entry; i++) {

        if (do_extract_currentfile(uf) != UNZ_OK) {
            return 1;
        }

        if ((i+1) < gi.number_entry) {
            err = unzGoToNextFile(uf);

            if (err != UNZ_OK) {
                printf("error %d with zipfile in unzGoToNextFile\n", err);
                return 1;
            }
        }
    }

    return 0;
}

int main(int argc, char *argv[]) 
{
    const char *zipfilename = NULL;
    const char *dirname     = NULL;
    unzFile    uf           = NULL;
    int        ret_value    = 0;

    if (argc != 3) {
        printf("Usage : %s [file.zip] [extract.dir]\n", argv[0]);
        return 0;
    }

    zipfilename = argv[1];
    dirname     = argv[2];

#ifdef USEWIN32IOAPI
    zlib_filefunc64_def ffunc;
#endif

#ifdef USEWIN32IOAPI
    fill_win32_filefunc64A(&ffunc);
    uf = unzOpen2_64(zipfilename, &ffunc);
#endif

    if (uf==NULL) {
        printf("Cannot open %s!\n", zipfilename);
        return 1;
    }

#ifdef _WIN32
    if ( _chdir(dirname)) {
#else
    if (chdir(dirname)) {
#endif
        printf("Error changing into %s, aborting\n", dirname);
        return 1;
    }

    ret_value = do_extract(uf);

    unzClose(uf);

    return ret_value;
}

