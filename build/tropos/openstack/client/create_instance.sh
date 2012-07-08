#!/bin/bash
euca-run-instances -k demo -n 1 -g default -t m1.tiny ami-00000002
