#/bin/bash

image_id=""
image_user=""
image_password=""
ip1=""
ip2=""
image_root=""
image_template=""
new_image_size="" #In G
original_image=""
image_keys_file=""
work_dir=""
software=""

function image_validate {

    if [[ ( -z $image_id || -z $image_user || -z $image_password || -z $image_root || -z $image_template || -z $original_image ) ]]; then
        echo "usage: customize_image.sh image-id=<image id> image-user=<image_user> image-password=<password> ip1=<load balancer ip (optional)> ip2=<host ip (optional)> image-root=<image_root> template=<template> image-size=<image_size in G (optional)> original-image=<original_image> image-keys-file=<image keys file (optional)> software=<software (optional)>"
        
        echo "usage example: customize_image.sh image-id=12345678 image-user=yang image-password=yang ip1=10.100.1.20 ip2=192.168.254.2 image-root=/opt/lxc template=lamp image-size=3G original-image=ubuntu-12.04-server-cloudimg-amd64.tar.gz"
        exit 1
    fi
}

for var in $@; do
    set -- `echo $var | tr '=' ' '`
    key=$1
    value=$2

    if [ $key = "image-id" ]; then
        image_id="$value"
        echo "image id:" $image_id
    fi

    if [ $key = "image-user" ]; then
        image_user="$value"
        echo "user:" $image_user
    fi
    
    if [ $key = "image-password" ]; then
        image_password="$value"
    fi
    
    if [ $key = "ip1" ]; then
        ip1="$value"
        echo "ip1:" $ip1
    fi
    
    if [ $key = "ip2" ]; then
        ip2="$value"
        echo "ip2:" $ip2
    fi
    
    if [ $key = "image-root" ]; then
        image_root="$value"
        echo "image root:" $image_root
    fi
    
    
    if [ $key = "template" ]; then
        image_template="$value"
        echo "template:" $image_template
    fi
    
    if [ $key = "image-size" ]; then
        new_image_size="$value"
        echo "image-size:" $new_image_size
    fi
    
    if [ $key = "original-image" ]; then
        original_image="$value"
        echo "original-image:" $original_image
    fi

    if [ $key = "image-keys-file" ]; then
        image_keys_file="$value"
        echo "keys file:" $image_keys_file
    fi
    
    if [ $key = "software" ]; then
        software="$value"
        echo "software:" $software
    fi

done


image_validate
work_dir="$image_root/$image_id"
img_dir="$work_dir/image"
original_img_dir="$image_root/original_image"

if [ -d ./$image_id ]; then
    rm -rf ./$image_id
fi

if [ -d ./$work_dir ]; then
    rm -rf ./$work_dir
fi


if [ ! -d $img_dir ]; then
    mkdir -p $img_dir
fi

pushd $img_dir
tar -zxf "$original_img_dir/$original_image"
popd

image_image=`ls $img_dir/*.img`
if [ -z $new_image_size ]; then
    echo "Image will not be resized since new image size is not provided"
else
    echo "Resizing the original image"
    fsck.ext3 -f $image_image
    resize2fs $image_image $new_image_size
fi

echo "Mount the original image"
./image_action.sh action=mount image-id=$image_id image-root=$image_root image-image=$image_image
echo "Original image mounted"

echo "Customizing the original image"

./image_action.sh action=create image-id=$image_id image-user=$image_user image-password=$image_password ip1=$ip1 ip2=$ip2 image-root=$image_root template=$image_template image-keys-file=$image_keys_file software=$software

echo "Archiving the new image"
pushd $work_dir/image
tar -zcf $original_image ./*
popd
echo "Unmounting"
./image_action.sh action=unmount image-id=$image_id image-root=$image_root

echo "Done"
