#!/bin/bash
gcc -lrabbitmq ./ads_listener.c -o ads_listener  ./ads_utils.c
