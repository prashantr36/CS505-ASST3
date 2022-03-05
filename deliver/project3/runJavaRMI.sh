echo "Setting up 5 Replicas..."
FILE=configuration/configs.txt
Server_Name=0
chmod 775 "$( cd "$( dirname "$0" )" && pwd )"/Gnutella-1/RMIClient/integration_test.py
find ../ -type f -name "*.txt" -exec touch {} +
while read line;do
	if [[ "$Server_Name" -eq 0 ]]; then
		(cd "/Gnutella-1/RMICoordinator" && java -jar RMICoordinator.jar $line &)
	else
		(cd "/Gnutella-1/RMIServer$Server_Name" && java -jar RMIServer.jar $line &)
	fi
	((Server_Name++))
done < $FILE
for i in {1..5}; do 
  printf '\r%2d' $i
  sleep 1
done
find ../ -type f -name "*.txt" -exec touch {} +
# Unit Test
(cd "/Gnutella-1/RMIClient" && sh runJavaRMIClient.sh);
# Integration test
cd "/Gnutella-1/RMIClient" && python3 "$( cd "$( dirname "$0" )" && pwd )"/Gnutella-1/RMIClient/integration_test.py