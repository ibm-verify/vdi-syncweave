/*
 * IBM Confidential
 * PID 5724-K74
 *
 * Copyright IBM Corp. 2023
 */

/*
 * This program is used to parse a TMSXML message file and produce the
 * corresponding properties file.  This program is designed to replace the
 * corresponding Java command to help improve the build performance.
 *
 * To build this program simply execute the following line:
 *  g++ -I/usr/include/libxml2 -o tmsxml_to_properties \
 #     tmsxml_to_properties.cpp -lxml2 -lxslt -lexslt
 */

#include <stdio.h>
#include <string.h>

#include <string>
#include <fstream>

#include <libxml/parser.h>
#include <libxslt/transform.h>
#include <libxslt/xsltutils.h>
#include <libexslt/exslt.h>

/******************************************************************************/

void processApos(std::string& line)
{
    size_t lastIdx = 0;
    size_t idx     = 0;

    /*
     * Find the next '{' in the string.
     */

    while ((idx = line.find('{', lastIdx)) != std::string::npos) {
        /*
         * Check to see if the prior character is a "'", but is not
         * "''"
         */

        if (idx == 0 || (line[idx-1] == '\'' && 
                        (idx == 1 || (line[idx-2] != '\'')))) {
            line.insert(idx++, 1, '\'');

            /*
             * Find the closing '}'.  We need to insert two apostrophies
             * if not already there.
             */

            int endIdx = line.find('}', idx);

            if (endIdx != std::string::npos) {
                if (line.compare(endIdx + 1, std::string::npos, "''") != 0) {
                    if (endIdx == line.length() - 1 
                                            || line[endIdx + 1] != '\'') {
                        line.insert(endIdx + 1, "''");
                    } else {
                        line.insert(endIdx + 1, 1, '\'');
                    }
                }
            }

            /*
             * Move past the closing '}'.
             */

            idx = endIdx + 1;
        }

        /*
         * Set the last index.
         */

        lastIdx = idx + 1;
    }
}

/******************************************************************************/

int main(int argc, char* argv[])
{
    /*
     * Check the command line usage.
     */

    if (argc != 4) {
        printf("Usage: %s [xml-file] [xsl-file] [out-file]\n", argv[0]);

        return 1;
    }

    /*
     * Parse the stylesheet.
     */

    xsltStylesheetPtr xsl = xsltParseStylesheetFile((const xmlChar*)argv[2]);

    if (xsl == NULL) {
        fprintf(stderr, "XSL file [%s] not loaded\n", argv[2]);

        return 1;
    }

    exsltRegisterAll();

    /*
     * Load the XML document.
     */

    xmlDocPtr doc = xmlParseFile(argv[1]);

    if (doc == NULL) {
        fprintf(stderr, "XML file [%s] not loaded\n", argv[1]);

        if (xsl) xsltFreeStylesheet(xsl);

        return 1;
    }

    /*
     * Set up our parameters which will be passed to the stylesheet
     * transform.
     */

    const char* params[] = {
        "preformat",  "TRUE", 
        "doubleapos", "TRUE", 
        "varonly",    "TRUE", 
        NULL
    };

    /*
     * Apply the transformation.
     */

    xmlDocPtr res = xsltApplyStylesheet(xsl, doc, params);
    if (res == NULL) {
        fprintf(stderr, "Error translating file [%s]\n", argv[1]);

        if (doc) xmlFreeDoc(doc);
        if (xsl) xsltFreeStylesheet(xsl);

        return 1;
    }

    /*
     * Unfortunately we cannot transform apostrophies correctly as the
     * transform relies on a TMSXML java class.  So, we instead process
     * this manually.  The rule is that all paramaters (i.e. '{.}') should
     * be surrounded by two apostrophies.
     */

    xmlChar* text    = 0x00;
    int      textLen = 0;

    if (xsltSaveResultToString(&text, &textLen, res, xsl) != 0) {
        fprintf(stderr, "Error saving the result to a string\n");

        if (res) xmlFreeDoc(res);
        if (doc) xmlFreeDoc(doc);
        if (xsl) xsltFreeStylesheet(xsl);

        return 1;
    }

    char* curLine = (char*)text;

    std::ofstream myfile;

    myfile.open(argv[3]);

    if (!myfile.is_open()) {
        fprintf(stderr, "Failed to open the output file: %s\n", argv[3]);

        if (res) xmlFreeDoc(res);
        if (doc) xmlFreeDoc(doc);
        if (xsl) xsltFreeStylesheet(xsl);

        return 1;
    }

    while (curLine) {
        char* nextLine = strchr(curLine, '\n');

        std::string line(curLine, 
                        nextLine ? nextLine - curLine : strlen(curLine));

        processApos(line);

        myfile << line << std::endl;

        curLine = nextLine ? (nextLine + 1) : NULL;
    }

    myfile.close();

    xmlFree(text);

    /*
     * Clean up.
     */

    if (res) xmlFreeDoc(res);
    if (doc) xmlFreeDoc(doc);
    if (xsl) xsltFreeStylesheet(xsl);

    return 0;
}

/******************************************************************************/

