#/bin/bash

work_dir=""
new_image=""
image_root="/tmp"
image_template="default"
software=""
new_image_size="" #In GB
action=""
original_image=""

function image_validate {

if [[ ( -z $action || ( -n $action && !( $action == "create" || $action == "pack" ) ) ) ]]; then
        echo "usage: mandatory parameter action with value create or pack should be provided"
        exit 1
    fi

if [[ ( -n $action && $action == "create" && -z $original_image ) ]]; then
        echo "usage: customize_image.sh -r <image root:default /tmp> -t <image template: default no template> -s <software> -n <new image size in Gbytes:default is original image size> create <path to original image>"
        exit 1
fi

if [[ ( -n $action && $action == "pack" && -z $new_image ) ]]; then
        echo "usage: customize_image.sh -r <image root:default /tmp> -t <image template: default no template> pack <new image archive name in tar.gz format>"
        exit 1
fi

}
while getopts r:t:s:n: opts
do
  case $opts in
    r)
        image_root=${OPTARG}
        ;;
    t)
        image_template=${OPTARG}
        ;;
    s)
        software=${OPTARG}
        ;;
    n)
        new_image_size=${OPTARG}
        ;;
    *)
        echo "Syntax: $(basename $0) -r <image root:default /tmp> -t <image template: default no template> -s <software> -n <new image size:default is original image size> <action> <path to original image>"
        exit 1
        ;;
  esac
done
shift $((OPTIND-1))
action=$1
if [[ $action == "create" ]]; then
    original_image=$2
elif [[ $action == "pack" ]]; then
    new_image=$2
fi

image_validate

work_dir="$image_root/$image_template"
img_dir="$work_dir/image"


if [[ $action == "create" ]]; then

    if [ -d ./$work_dir ]; then
        rm -rf ./$work_dir
    fi

    mkdir -p $work_dir

    if [ ! -d $img_dir ]; then
        mkdir -p $img_dir
    fi

    pushd $img_dir
    tar -zxf $original_image
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
    ./image_action.sh action=mount image-template=$image_template image-root=$image_root image-image=$image_image
    echo "Original image mounted"

    if [[ !( $image_template == "default" ) ]]; then
    	echo "Customizing the original image"
    	./image_action.sh action=create image-template=$image_template image-root=$image_root software=$software
    fi

fi

if [[ $action == "pack" ]]; then
    echo "Archiving the new image"
    pushd $work_dir/image
    tar -zcf $new_image ./*
    popd
    echo "Unmounting"
    ./image_action.sh action=unmount image-template=$image_template image-root=$image_root
fi
echo "Done"

