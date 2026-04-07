# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

# 参考 https://ejin66.github.io/2018/01/08/android-ndk.html
# Ejin NDK 开发总结

cmake_minimum_required(VERSION 3.7)

#设置生成的so动态库最后输出的路径,
#${CMAKE_HOME_DIRECTORY} = src/main/cpp
#${PROJECT_SOURCE_DIR} = src/main/java/
#${PROJECT_SOURCE_DIR}/../jniLibs/ == src/main/jniLibs
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

#配置多个文件。在add_library中使用，指生成的library中所包括的文件。
#file(GLOB SOURCES "${CMAKE_HOME_DIRECTORY}/*.c" "${CMAKE_HOME_DIRECTORY}/*.cpp")
#多级目录, GLOB_RECURSE命令会收集目录及其子目录的所有符合过文件滤器的文件，并作为变量SOURCES保存
file(GLOB_RECURSE sources CONFIGURE_DEPENDS
        "${CMAKE_HOME_DIRECTORY}/source/*.c"
        "${CMAKE_HOME_DIRECTORY}/source/*.cpp"
        "${CMAKE_HOME_DIRECTORY}/include/*.c"
        "${CMAKE_HOME_DIRECTORY}/include/*.cpp"
        "${CMAKE_HOME_DIRECTORY}/facade/*.c"
        "${CMAKE_HOME_DIRECTORY}/facade/*.cpp"
        )

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

#list(REMOVE_ITEM sources ${CMAKE_HOME_DIRECTORY}/source/example8.cpp)

# Specifies a path to native header files.
add_library( # Specifies the name of the library.
        wfdb
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        ${sources}
        )

#以 DIRECTORY 的方式，获取 ${CMAKE_HOME_DIRECTORY} 目录的父目录，以PARENT_DIR变量返回
get_filename_component(
        PARENT_DIR
        ${CMAKE_HOME_DIRECTORY}
        DIRECTORY)
#
#set (CMAKE_SHARED_LINKER_FLAGS "-Wl,--as-needed -Wl,-no-undefined")
#set_target_properties(
#        wfdb
#        PROPERTIES
#        CXX_STANDARD 11
#        COMPILE_FLAGS "-Wall -Wextra"
#)


# Specifies a path to native header files.
# if you want: #include "my_lib/foo.h"
# then use: include_directories(${CMAKE_SOURCE_DIR})
# if you want: #include "foo.h"
# then use: include_directories(${CMAKE_SOURCE_DIR}/my_lib)
# 即: 默认情况下，如不加以指定，所有的源文件都是以CMAKE_SOURCE_DIR为路径开始搜索头文件
# 比如source文件夹下的 aes128_s.c文件中， #include <aes128_s.h>，
# 会去 ../source 路径中找该文件，如果找不到，则报错
# "" 会在当前目录中找，找不到则取系统库找
# <> 去系统库找，这里所谓系统库，则默认为 CMAKE_HOME_DIRECTORY
include_directories(
        ${CMAKE_HOME_DIRECTORY}/include
        ${CMAKE_HOME_DIRECTORY}/facde
        ${CMAKE_HOME_DIRECTORY}/source
        ${CMAKE_HOME_DIRECTORY}/source/wfdb
)

find_library( # Defines the name of the path variable that stores the
        # location of the NDK library.
        log-lib

        # Specifies the name of the NDK library that
        # CMake needs to locate.
        log
        )

# Links your native library against one or more other native libraries.
target_link_libraries( # Specifies the target library.
        wfdb
        # Links the log library to the target library.
        ${log-lib}
        )
# 单独设置链接器选项(指定16KB对齐)
target_link_options(wfdb PRIVATE -Wl,-z,max-page-size=16384)