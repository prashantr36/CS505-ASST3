echo "[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[["
echo "]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]"
echo "---------------------------BASIC TEST----------------------------"
echo "[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[["
echo "]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]"

echo "*******************  1.GET test.txt ***********************"
java  -jar -DclientId=1 -DserverChoice=3 RMIClient.jar GET f1ed5429-cf9d-4e98-b0af-35af9c50c225.txt 
java  -jar -DclientId=1 -DserverChoice=4 RMIClient.jar GET f1ed5429-cf9d-4e98-b0af-35af9c50c225.txt
java  -jar -DclientId=1 -DserverChoice=2 RMIClient.jar GET f1ed5429-cf9d-4e98-b0af-35af9c50c225.txt
java  -jar -DclientId=1 -DserverChoice=3 RMIClient.jar GET fbf15d1f-67b4-465e-a342-3bfcba0d4ac1.txt
java  -jar -DclientId=1 -DserverChoice=3 RMIClient.jar GET will_this_work.txt
java  -jar -DclientId=1 -DserverChoice=4 RMIClient.jar GET will_this_work.txt
java  -jar -DclientId=1 -DserverChoice=5 RMIClient.jar GET will_this_work.txt
java  -jar -DclientId=1 -DserverChoice=5 RMIClient.jar RETRIEVE alpha.txt

echo "//////// end of SIMULATION /////////////////////"