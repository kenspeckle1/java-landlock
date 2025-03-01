all:
ifeq ($(JAVA_HOME),"")
	echo "JAVA_HOME not set"; exit 1
endif
	if [ "${JAVA_HOME}" == "" ]; then echo "JAVA_HOME not set"; exit 1; else gcc -I"${JAVA_HOME}/include" -I"${JAVA_HOME}/include/linux" -o src/main/resources/libjavalandlock.so -shared -fPIC src/main/cpp/landlock_jni.c; fi

clean:
	[ -f src/main/resources/libjavalandlock.so ] && rm src/main/resources/libjavalandlock.so || echo "No need to remove library"