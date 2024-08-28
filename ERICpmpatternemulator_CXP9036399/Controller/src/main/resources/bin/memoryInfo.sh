#!/bin/bash
# ********************************************************************
# Ericsson Inc.	                                     			SCRIPT
# ********************************************************************
#
# (c) Ericsson Inc. 2018 - All rights reserved.
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#
# ********************************************************************
# Name    : memoryInfo.sh
# Purpose : This script to used to display memory information
#
# Usage   : ${INSTALL_DIR}/bin/memoryInfo.sh
#
# ********************************************************************

PLATFORM_TYPE=`uname -s`
if [ $PLATFORM_TYPE == "Linux" ] ; then
	# Available memory
	memory=`cat /proc/meminfo | grep "MemTotal" | head -1 | awk 'BEGIN {FS=" "} {print $2}'`
	gb_memory=`echo "scale=2; $memory/1024/1024" | bc -l`
	
	# Free memory
	pagesize=`getconf PAGE_SIZE`
	sar_freemem=`cat /proc/meminfo | grep "MemFree" | head -1 | awk 'BEGIN {FS=" "} {print $2}'`
	sar_cached=`cat /proc/meminfo | grep "Cached" | head -1 | awk 'BEGIN {FS=" "} {print $2}'`
	gb_freemem=`echo "scale=2; ($sar_freemem+$sar_cached)/1024/1024" | bc -l`
	
	# Used Memory
	gb_usedmem=`echo "scale=2; $gb_memory-$gb_freemem" | bc -l`
	
	#For Swap
	swap_total=`cat /proc/meminfo | grep "SwapTotal" | head -1 | awk 'BEGIN {FS=" "} {print $2}'`
	gb_swap_total=`echo "scale=2; $swap_total/1024/1024" | bc -l`
	swap_free=`cat /proc/meminfo | grep "SwapFree" | head -1 | awk 'BEGIN {FS=" "} {print $2}'`
	gb_swap_free=`echo "scale=2; $swap_free/1024/1024" | bc -l`
	
	# Used Swap
	gb_swap_used=`echo "scale=2; $gb_swap_total-$gb_swap_free" | bc -l`

else
	# Available memory
	memory=`/usr/sbin/prtconf | grep Memory | head -1 | awk 'BEGIN {FS=" "} {print $3}'`
	gb_memory=`echo "scale=2; $memory/1024" | bc -l`
	
	# Free memory
	pagesize=`pagesize`
	kb_pagesize=`echo "scale=2; $pagesize/1024" | bc -l`
	sar_freemem=`sar -r 1 1 | tail -1 | awk 'BEGIN {FS=" "} {print $2}'`
	gb_freemem=`echo "scale=2; $kb_pagesize*$sar_freemem/1024/1024" | bc -l`
	
	# Used Memory
	gb_usedmem=`echo "scale=2; $gb_memory-$gb_freemem" | bc -l`
	
	#For Swap
	/usr/sbin/swap -l |awk '{ print $4 }'|grep -v blocks > temp.swapl
	/usr/sbin/swap -l |awk '{ print $5}'|grep -v free > free.swap1
	SWP=$(echo $(tr -s '\n' '+' < temp.swapl)0 | bc)
	TSWP=$(echo "$SWP" "/" "2" |bc)
	gb_swap_total=$(echo "$TSWP" "/" "1024" "/" "1024" |bc)
	gb_swap_free=$(echo "scale=0;`awk '{total += $NF} END { print total }' free.swap1` "/" "2" "/" "1024" "/" "1024" "|bc)
	rm temp.swapl
	rm free.swap1
fi

# Final Output
echo "Avai Mem: $gb_memory GB"
echo "Free Mem: $gb_freemem GB"
echo "Used Mem: $gb_usedmem GB"
echo "Total Swap Space : $gb_swap_total GB"
echo "Free Swap Space : $gb_swap_free GB"
#echo "Used Swap Space : $gb_swap_used GB"

