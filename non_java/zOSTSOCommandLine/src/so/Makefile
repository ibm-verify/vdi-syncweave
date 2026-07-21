CC     = c89
DEBUG  = -O
JAVA_DIR = /usr/lpp/java/J1.4
LFLAGS = -W l,DLL
CFLAGS = -D MVS -W c,LONGLONG,DLL,exportall -I$(JAVA_DIR)/include
LIB_NAME = libtdiappc

JDK_LIB       = $(JAVA_DIR)/bin/classic/libjvm.x

OBJS = zoscmd.o\
       util.o

TARGETS  = $(OBJS)

all : $(TARGETS) tdiTSOExec

zoscmd.o : zoscmd.c
	$(CC) $(CFLAGS) -c zoscmd.c

util.o : util.c
	$(CC) $(CFLAGS) -c util.c

tdiTSOExec : $(TARGETS)
	$(CC) $(LFLAGS) -o $(LIB_NAME).so $(TARGETS) $(JDK_LIB)

clean :
	rm -f $(TARGETS) $(LIB_NAME).x $(LIB_NAME).so
