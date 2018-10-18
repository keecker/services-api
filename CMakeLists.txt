cmake_minimum_required(VERSION 3.4.1)

project("sharedmemory")

add_definitions("-Wall" "-Wextra" "-Werror")
find_library(LIBLOG "log")

file(GLOB_RECURSE SOURCES "src/main/jni/*.cpp")
add_library("${PROJECT_NAME}" SHARED ${SOURCES})
target_link_libraries("${PROJECT_NAME}" ${LIBLOG})
