cmake_minimum_required(VERSION 3.31)
project(rish)

find_package(cxx REQUIRED CONFIG)

add_library(rish SHARED
        rish.cpp
)

target_link_libraries(rish
        cxx::cxx
        log
)
