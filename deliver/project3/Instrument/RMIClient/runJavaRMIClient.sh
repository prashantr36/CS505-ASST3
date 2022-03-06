echo "[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[["
echo "]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]"
echo "---------------------------BASIC TEST----------------------------"
echo "[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[["
echo "]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]"

echo "*******************  1.GET test.txt ***********************"
java  -jar -DclientId=1 -DserverChoice=1 RMISuperPeerClient.jar GET f1ed5429-cf9d-4e98-b0af-35af9c50c225.txt 
java  -jar -DclientId=1 -DserverChoice=1 RMISuperPeerClient.jar GET f1ed5429-cf9d-4e98-b0af-35af9c50c225.txt
java  -jar -DclientId=1 -DserverChoice=1 RMISuperPeerClient.jar GET f1ed5429-cf9d-4e98-b0af-35af9c50c225.txt
java  -jar -DclientId=1 -DserverChoice=1 RMISuperPeerClient.jar GET fbf15d1f-67b4-465e-a342-3bfcba0d4ac1.txt
java  -jar -DclientId=1 -DserverChoice=1 RMISuperPeerClient.jar GET will_this_work.txt
java  -jar -DclientId=1 -DserverChoice=1 RMISuperPeerClient.jar GET will_this_work.txt
java  -jar -DclientId=1 -DserverChoice=1 RMISuperPeerClient.jar GET will_this_work.txt
java  -jar -DclientId=1 -DserverChoice=1 RMISuperPeerClient.jar RETRIEVE alpha.txt

echo "//////// end of SIMULATION /////////////////////"