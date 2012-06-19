#!/bin/bash
gcc ./container_action.c -o ./container_action
sudo chown root.root ./container_action
sudo chmod u+s ./container_action

