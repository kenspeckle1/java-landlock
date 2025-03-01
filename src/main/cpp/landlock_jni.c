#include <linux/landlock.h>
#include <linux/limits.h>
#include <linux/prctl.h>
#include <sys/prctl.h>
#include <stdio.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>

#include <jni.h>
#include <sys/syscall.h>
#include <unistd.h>
#include <linux/landlock.h>

#ifndef landlock_create_ruleset
static inline int landlock_create_ruleset(const struct landlock_ruleset_attr *const attr, const size_t size, const __u32 flags)
{
	return syscall(__NR_landlock_create_ruleset, attr, size, flags);
}
#endif

#ifndef landlock_add_rule
static inline int landlock_add_rule(const int ruleset_fd, const enum landlock_rule_type rule_type, const void *const rule_attr, const __u32 flags)
{
	return syscall(__NR_landlock_add_rule, ruleset_fd, rule_type, rule_attr, flags);
}
#endif

#ifndef landlock_restrict_self
static inline int landlock_restrict_self(const int ruleset_fd, const __u32 flags)
{
	return syscall(__NR_landlock_restrict_self, ruleset_fd, flags);
}
#endif

/* Begin JNI functions */

JNIEXPORT jint JNICALL Java_org_kenspeckle_landlock_RuleSet_getLandlockAbiVersion(JNIEnv *, jobject)
{
    return landlock_create_ruleset(NULL, 0, LANDLOCK_CREATE_RULESET_VERSION);
}


JNIEXPORT jint JNICALL Java_org_kenspeckle_landlock_RuleSet_createRuleSetNative(JNIEnv *, jobject, jlong allowed_fs_flags, jlong allowed_net_flags)
{
	// by default deny everything except the specified flags
	struct landlock_ruleset_attr ruleset_attr = {
		.handled_access_fs =
			LANDLOCK_ACCESS_FS_EXECUTE | LANDLOCK_ACCESS_FS_WRITE_FILE |
			LANDLOCK_ACCESS_FS_READ_FILE | LANDLOCK_ACCESS_FS_READ_DIR |
			LANDLOCK_ACCESS_FS_REMOVE_DIR | LANDLOCK_ACCESS_FS_REMOVE_FILE |
			LANDLOCK_ACCESS_FS_MAKE_CHAR | LANDLOCK_ACCESS_FS_MAKE_DIR |
			LANDLOCK_ACCESS_FS_MAKE_REG | LANDLOCK_ACCESS_FS_MAKE_SOCK |
			LANDLOCK_ACCESS_FS_MAKE_FIFO | LANDLOCK_ACCESS_FS_MAKE_BLOCK |
			LANDLOCK_ACCESS_FS_MAKE_SYM | LANDLOCK_ACCESS_FS_REFER |
			LANDLOCK_ACCESS_FS_TRUNCATE | LANDLOCK_ACCESS_FS_IOCTL_DEV,
		.handled_access_net =
			LANDLOCK_ACCESS_NET_BIND_TCP | LANDLOCK_ACCESS_NET_CONNECT_TCP,
	};
	ruleset_attr.handled_access_fs &= ~allowed_fs_flags;
	ruleset_attr.handled_access_net &= ~allowed_net_flags;

	// Remove flags that are not supported by this version of the linux kernel
	int abi = landlock_create_ruleset(NULL, 0, LANDLOCK_CREATE_RULESET_VERSION);
	switch (abi) {
	case 1:
		/* Removes LANDLOCK_ACCESS_FS_REFER for ABI < 2 */
		ruleset_attr.handled_access_fs &= ~LANDLOCK_ACCESS_FS_REFER;
		__attribute__((fallthrough));
	case 2:
		/* Removes LANDLOCK_ACCESS_FS_TRUNCATE for ABI < 3 */
		ruleset_attr.handled_access_fs &= ~LANDLOCK_ACCESS_FS_TRUNCATE;
		__attribute__((fallthrough));
	case 3:
		/* Removes network support for ABI < 4 */
		ruleset_attr.handled_access_net &=
				~(LANDLOCK_ACCESS_NET_BIND_TCP | LANDLOCK_ACCESS_NET_CONNECT_TCP);
		__attribute__((fallthrough));
	case 4:
		/* Removes LANDLOCK_ACCESS_FS_IOCTL_DEV for ABI < 5 */
		ruleset_attr.handled_access_fs &= ~LANDLOCK_ACCESS_FS_IOCTL_DEV;
	}

	return landlock_create_ruleset(&ruleset_attr, sizeof(ruleset_attr), 0);
}

/*
 * Class:		 org_kenspeckle_landlock_RuleSet
 * Method:		addFsRuleNative
 * Signature: (JLjava/lang/String;)Z
 * returns fd/err if there was an error
 */
JNIEXPORT jint JNICALL Java_org_kenspeckle_landlock_RuleSet_addFsRuleNative(JNIEnv * env, jobject, jlong ruleset_fd, jlong allowed_fs_flags, jstring jpath)
{
	int err;
	struct landlock_path_beneath_attr path_beneath = {
		.allowed_access = (unsigned long long int) allowed_fs_flags
	};
	const char *current_path = (*env)->GetStringUTFChars(env, jpath, 0);
	path_beneath.parent_fd = open(current_path, __O_PATH | O_CLOEXEC);

	if (path_beneath.parent_fd < 0)
	{
		perror("Failed to open file\n");
		(*env)->ReleaseStringUTFChars(env, jpath, current_path);
		close(ruleset_fd);
		return path_beneath.parent_fd;
	}

	(*env)->ReleaseStringUTFChars(env, jpath, current_path);
	err = landlock_add_rule(ruleset_fd, LANDLOCK_RULE_PATH_BENEATH, &path_beneath, 0);
	close(path_beneath.parent_fd);

	if (err)
	{
		perror("Failed to update ruleset: %d\n", err);
		close(ruleset_fd);
		return err;
	}

	return 0;
}

/*
 * Class:		 org_kenspeckle_landlock_RuleSet
 * Method:		addNetRuleNative
 * Signature: (JI)Z
 */
JNIEXPORT jint JNICALL Java_org_kenspeckle_landlock_RuleSet_addNetRuleNative(JNIEnv *,jobject, jlong ruleset_fd, jlong allowed_flags, jint port)
{
	struct landlock_net_port_attr net_port = {
		.allowed_access = (unsigned long long)allowed_flags,
		.port = (unsigned long long)port,
	};

	int err = landlock_add_rule(ruleset_fd, LANDLOCK_RULE_NET_PORT, &net_port, 0);
	if (err)
	{
		perror("There was an error when trying to add net rule for port %d: ", port);
		if (allowed_flags & LANDLOCK_ACCESS_NET_BIND_TCP)
			perror("BIND_TCP");
		
		if (allowed_flags & LANDLOCK_ACCESS_NET_CONNECT_TCP)
			perror("CONNECT_TCP");

		perror("\nErr: %d\n", err);
		return err;
	}
	return err;
}


/*
 * Class:		 org_kenspeckle_landlock_RuleSet
 * Method:		restrictSelf
 * Signature: (JI)Z
 */
JNIEXPORT jint JNICALL Java_org_kenspeckle_landlock_RuleSet_restrictSelfNative(JNIEnv *,jobject, jlong ruleset_fd)
{
	if (prctl(PR_SET_NO_NEW_PRIVS, 1, 0, 0, 0))
	{
		perror("Failed to restrict privileges");
		close(ruleset_fd);
		return 1;
	}

	int err = landlock_restrict_self(ruleset_fd, 0);
	if (err)
	{
		perror("Failed to enforce ruleset");
		close(ruleset_fd);
		return 2;
	}
	close(ruleset_fd);
	return err;
}


