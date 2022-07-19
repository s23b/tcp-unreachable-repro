#include <jni.h>
#include <string.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <android/log.h>

#define TAG "NetUnreachC"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C"
JNIEXPORT void JNICALL Java_com_example_networkunreachablerepro_MainActivity_cDial(
        JNIEnv* env,
        jobject thiz,
        jstring host,
        jint port) {

    int sockfd, connfd;
    struct sockaddr_in servaddr;
    jclass ex = env->FindClass("java/net/SocketException");

    LOGE("Creating socket\n");
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd == -1) {
        LOGE("Socket creation failed\n");
        env->ThrowNew(ex, "Socket creation failed");
    } else {
        LOGE("Socket successfully created: %d\n", sockfd);
    }

    bzero(&servaddr, sizeof(servaddr));

    // assign IP, PORT
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = inet_addr(env->GetStringUTFChars(host, NULL));
    servaddr.sin_port = htons(port);

    LOGE("Connecting to socket\n");
    int conn = connect(sockfd, (struct sockaddr*)&servaddr, sizeof(servaddr));
    if (conn != 0) {
        LOGE("Connection failed: %d\n", conn);
        env->ThrowNew(ex, "Connection failed");
    } else {
        LOGE("Connection successful\n");
    }

    LOGE("Closing socket\n");
    close(sockfd);
    LOGE("Socket closed\n");
}
