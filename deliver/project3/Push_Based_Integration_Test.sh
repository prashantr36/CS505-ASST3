echo "Setting up 10 Gnutella..."
FILE=configuration/configs.txt
Server_Name=0
Gnutella_Name=0
export PUSH_BASED_CONSISTENCY=TRUE
export TTR=450
export PROFILE=FALSE
chmod 775 "$( cd "$( dirname "$0" )" && pwd )"/Instrument/RMIClient/push_based_integration_test.py
find ../ -type f -name "*.txt" -exec touch {} +
while read line;do
	if [[ "$line" =~ ^# ]]; then
		continue
	fi
	if [[ "$line" =~ ^Topology ]]; then
		continue
	fi
	if [[ "$line" =~ ^Gnutella ]]; then
		((++Gnutella_Name))
		((Server_Name = 0))
		continue
	fi
	[ -z "$line" ] && continue
	[[ -z "${line// }" ]] && continue
	if [[ "$Gnutella_Name" -eq 0 ]]; then
		continue
	fi
	
	if [[ "$Server_Name" -eq 0 ]]; then
		(cd "Gnutella-$Gnutella_Name/RMICoordinator" && java -jar SuperPeer.jar $line &)
		((++Server_Name))
	else
		(cd "Gnutella-$Gnutella_Name/RMIServer$Server_Name" && java -jar LeafNode.jar $line &)
		((++Server_Name))
	fi
done < $FILE
for i in {1..5}; do 
  printf '\r%2d' $i
  sleep 1
done
find ../ -type f -name "*.txt" -exec touch {} +
# Integration test
echo "PUSH BASED CONSISTENCY Integ testing running..."
cd "Instrument/RMIClient" && python3 "$( cd "$( dirname "$0" )" && pwd )"/Instrument/RMIClient/push_based_integration_test.py