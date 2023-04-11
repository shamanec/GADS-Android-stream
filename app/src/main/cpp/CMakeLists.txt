cmake_minimum_required(VERSION 3.4.1)

add_library(native-lib SHARED native-lib.cpp)

find_library(log-lib log)
find_library(jpeg-turbo-lib jpeg)

include_directories(${CMAKE_SOURCE_DIR})

target_link_libraries(native-lib ${log-lib} ${jpeg-turbo-lib})
