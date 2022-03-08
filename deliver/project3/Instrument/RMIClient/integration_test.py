#!/usr/bin/env python3
import os
import random
import sys
import unittest
import random
import uuid
import string
import filecmp
import subprocess
from base64 import decode

class TestPeerToPeerMethods(unittest.TestCase):
    
    def get_parent_dir(directory):
            import os
            return os.path.dirname(directory)
    
    def setUp(self):
        self.new_files = []
        length = 10000
        letters = string.ascii_lowercase
        print('setup')
        def get_parent_dir(directory):
            import os
            return os.path.dirname(directory)
        for dirs in os.walk((get_parent_dir(get_parent_dir(os.getcwd()))) + "/Gnutella-1/RMIServer1/files"):
            d = dirs[0]
            print(d)
            for i in range(4):
                filename = str(uuid.uuid4()) + ".txt"
                print(filename)
                self.new_files.append(filename)
                size = random.randint(1, 100)
                with open(os.path.join(d, filename), "w") as f:
                    f.write(''.join(random.choice(letters) for i in range(length)))
        print("Added NEW TEST FILES ", self.new_files)
    
    
    def test_a_new_file_gets_added_single_gnutella_from_pa1(self):
        encoding = 'utf-8'
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=4', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('1. [Executed RETRIEVE AS Server 4 output:', out, ']')
        #assert 'RETRIEVE :No such file on any peer' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=3', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('2. [Executed RETRIEVE AS Server 3 output:', out, ']')
        #assert 'No such file on any peer' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=2', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'SEARCH',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('3. [Executed SEARCH AS Server 2 output:', out, ']')
        assert 'SEARCH :localhost:2345' in out
        
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=1', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'REGISTRY',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('5. [Executed REGISTER AS Server 1 output:', out, ' for file ', self.new_files[0], ']')
        assert 'ACCEPT' in out
                
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=2', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'SEARCH',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('6. [Executed SEARCH AS Server 2 output:', out, ']') 
        assert 'SEARCH :localhost:2345' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=3', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('7. [Executed RETRIEVE AS Server 3 output:', out, ']') 
        assert 'RETRIEVE :File ' +self.new_files[0] + ' Downloaded' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=3', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('8. [Executed RETRIEVE AS Server 3 output:', out, ']')
        #assert 'java.io.FileNotFoundException: files/' + self.new_files[0] +  ' (No such file or directory)' in out
        
        
        
        
        
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=3', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('9. [Executed RETRIEVE AS Server 3 output:', out, ']')
        
        
        
        
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=2', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'SEARCH',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('10. [Executed SEARCH AS Server 2 output:', out, ']') 
        assert 'SEARCH :localhost:2345' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=1', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'DEREGISTER',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('11. [EXECUTED REGISTRY AS Server 1 output:', out, ']')
        assert 'ACCEPT' in out
        
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=2', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'DEREGISTER',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('12. [EXECUTED DEREGISTRY AS Server 2 output:', out, ']')
        assert 'ACCEPT' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=3', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'DEREGISTER',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('13. [EXECUTED DEREGISTRY AS Server 3 output:', out, ']')
        assert 'ACCEPT' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=4', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'DEREGISTER',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('14. [EXECUTED DEREGISTRY AS Server 4 output:', out, ']')
        assert 'ACCEPT' in out
        
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=1', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'DEREGISTER',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('15. [EXECUTED DEREGISTRY AS Server 1 output:', out, ']')
        assert 'ACCEPT' in out
        
        encoding = 'utf-8'
        
    def test_a_file_gets_downloaded_on_different_cluster(self):
        def get_parent_dir(directory):
            import os
            return os.path.dirname(directory)
        
        encoding = 'utf-8'        
        # Files are always added to the Gnutella server 1 , RMI Server 1
        # We go and check the file for existence
        # Check THE GNUTELLA-2 cluster files folder and it should be downloaded there
        # On executing the QUERY_MESSAGE command.
        # We also compare the file contents.
        
        file_a_location = None
        for dirs in os.walk((get_parent_dir(get_parent_dir(os.getcwd()))) + "/Gnutella-1/RMIServer1/files"):
            d = dirs[0]
            file_a_location = os.path.join(d, self.new_files[0])
        
        file_b_location = None
        for dirs in os.walk((get_parent_dir(get_parent_dir(os.getcwd()))) + "/Gnutella-2/RMIServer4/files"):
            d = dirs[0]
            file_b_location = os.path.join(d, self.new_files[0])
        
        
        assert os.path.exists(file_a_location)
        assert not os.path.exists(file_b_location) # This will be downloaded by the program.
        
        encoding = 'utf-8'
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=2',
                                               '-DserverChoice=4', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'QUERY_MESSAGE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('1. [Executed QUERY_MESSAGE AS to RETREIVE', self.new_files[0], ' On GNUTELLA-2, RMIServer4 output:', out, ']')
        
        assert os.path.exists(file_a_location)
        assert os.path.exists(file_b_location)
        assert filecmp.cmp(file_a_location, file_b_location)

def suite():
    suite = unittest.TestSuite()
    suite.addTest(TestPeerToPeerMethods('test_a_new_file_gets_added_single_gnutella_from_pa1'))
    suite.addTest(TestPeerToPeerMethods('test_a_file_gets_downloaded_on_different_cluster'))
    return suite

if __name__ == '__main__':
    runner = unittest.TextTestRunner()
    runner.run(suite())