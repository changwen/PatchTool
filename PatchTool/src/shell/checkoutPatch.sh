#!/bin/bash

#获取补丁包的位置
i=1
for content in $@
do
   if [ $content == "-p" ]; then
      shift $i
      patchPath=$1
   fi

   i=$[ $i+1 ]
done

#获取新版本的位置
j=1
for content in $@
do
   if [ $content == "-n" ]; then
      shift $j
      newPath=$1
   fi

   j=$[ $j+1 ]
done



patchList=${patchPath}"/patchList.txt"

cat $patchList | while read line
do
   first=`echo $line | cut -d ' ' -f 1`
   second1=`echo $line | cut -d ' ' -f 2`
   second=$(echo $second1 | tr -d "\n\r")   #截取的最后一列有换行符，需要去除

   newFile=${newPath}${second}
   patchFile=${patchPath}${second}

   if [ "$first" == "add" ] || [ "$first" == "modify" ]
   then

    diff  $patchFile $newFile
      if [ "${?}" != "0" ]
      then
         echo "Generate a patch package failed! Please try again"
         exit 1
      fi

   fi
done
