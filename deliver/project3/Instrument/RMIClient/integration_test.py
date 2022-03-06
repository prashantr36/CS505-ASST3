#!/usr/bin/env python3
import os
import random
import sys
import unittest
import random
import uuid
import string
import subprocess
from base64 import decode

class TestPeerToPeerMethods(unittest.TestCase):
    
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
    
    def test_a_new_file_gets_added(self):
        encoding = 'utf-8'
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=3', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('1. [Executed RETRIEVE AS Server 3 output:', out, ']')
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
        assert 'RETRIEVE :File ' +self.new_files[0] + ' Downloaded' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=3', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'RETRIEVE',
                                                'unknown.txt',stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('7. [Executed RETRIEVE AS Server 3 output:', out, ']')
        #assert 'java.io.FileNotFoundException: files/' + self.new_files[0] +  ' (No such file or directory)' in out
        
        
        
        
        
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=3', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('8. [Executed RETRIEVE AS Server 3 output:', out, ']')
        
        
        
        
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=2', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'SEARCH',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('9. [Executed SEARCH AS Server 2 output:', out, ']') 
        assert 'SEARCH :localhost:2345' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=1', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'DEREGISTER',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        #print('7. [EXECUTED REGISTRY AS Server 2 output:', out, ']')
        assert 'ACCEPT' in out
        
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=2', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'DEREGISTER',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('10. [EXECUTED DEREGISTRY AS Server 2 output:', out, ']')
        assert 'ACCEPT' in out
        
        encoding = 'utf-8'
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=2', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('11. [Executed RETRIEVE AS Server 2 output:', out, ']')
        assert 'RETRIEVE :No such file on any peer' in out
        
def suite():
    suite = unittest.TestSuite()
    suite.addTest(TestPeerToPeerMethods('test_a_new_file_gets_added'))
    return suite

if __name__ == '__main__':
    runner = unittest.TextTestRunner()
    runner.run(suite())