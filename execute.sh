#!/bin/sh

usage() {
	echo "usage: $0 [ -h ] [ -g ] [ -d ] [ -x ] [ -r <repository path> ]"
	echo "-h : help"
	echo "-g : gui"
	echo "-d : ccnd"
	echo "-x : 64-bit Architecture"
	echo "-r : ccnr"
	exit 1
}

MAINCLASS="NDNDropbox"
LIBRARY="./NDNDropbox/libs/"

while getopts hgdr:x OPT; do
	case "$OPT" in
		h)
			usage
			;;
		g)
			MAINCLASS="NDNDriveGUI"
			;;
		d)
			ccndstop
			ccndstart
			;;
		r)
			( mkdir -p $OPTARG; cd $OPTARG; ccnr & ) 
			;;
		x)
			LIBRARY="./NDNDropbox/libs/64bit/"
			;;
		\?) 
			echo "Invalid Option..."
			usage
			;;
		*)
			echo "Invalid Argument..."
			usage
			;;
	esac
done

# Execute Application
java -Djava.library.path=$LIBRARY -classpath ./NDNDropbox/libs/ccn.jar:./NDNDropbox/libs/bcprov-jdk16-143.jar:./NDNDropbox/libs/commons-codec-1.7.jar:./NDNDropbox/libs/commons-io-2.4.jar:./NDNDropbox/libs/jnotify-0.94.jar:./NDNDropbox/bin $MAINCLASS
