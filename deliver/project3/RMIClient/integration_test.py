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
        def get_parent_dir(directory):
            import os
            return os.path.dirname(directory)

        for dirs in os.walk(get_parent_dir(os.getcwd()) + "/RMIServer1/files"):
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
                                               '-DserverChoice=4', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMIClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('1. [Executed RETRIEVE AS Server 4 output:', out, ']')
        #assert 'RETRIEVE :No such file on any peer' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=4', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMIClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('2. [Executed RETRIEVE AS Server 4 output:', out, ']')
        #assert 'No such file on any peer' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=3', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMIClient.jar'), 'SEARCH',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('3. [Executed SEARCH AS Server 3 output:', out, ']')
        assert 'SEARCH :localhost:2345' in out
        
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=2', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMIClient.jar'), 'REGISTRY',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('5. [Executed REGISTER AS Server 2 output:', out, ' for file ', self.new_files[0], ']')
        assert 'ACCEPT' in out
        
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=4', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMIClient.jar'), 'REGISTRY',
                                                "invalid.txt"],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        if out:
            out = str(out.decode(encoding))
        if err:
            err = str(err.decode(encoding))
        print('6. [Executed REGISTER AS Server 2 output:', out, ' for file ', 'invalid.txt', err , ']')
        assert 'no such file on server' in out
                
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=3', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMIClient.jar'), 'SEARCH',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('7. [Executed SEARCH AS Server 3 output:', out, ']') 
        assert 'SEARCH :localhost:2345' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=4', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMIClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        assert 'RETRIEVE :File ' +self.new_files[0] + ' Downloaded' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=4', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMIClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('8. [Executed RETRIEVE AS Server 4 output:', out, ']')
        assert 'RETRIEVE :File ' +self.new_files[0] + ' Downloaded' in out
        
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=4', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMIClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('9. [Executed RETRIEVE AS Server 4 output:', out, ']')
        
        
        
        
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=3', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMIClient.jar'), 'SEARCH',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('10. [Executed SEARCH AS Server 3 output:', out, ']') 
        assert 'SEARCH :localhost:2345' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=2', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMIClient.jar'), 'DEREGISTER',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        #print('7. [EXECUTED REGISTRY AS Server 2 output:', out, ']')
        assert 'ACCEPT' in out
        
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=4', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMIClient.jar'), 'DEREGISTER',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('11. [EXECUTED REGISTRY AS Server 2 output:', out, ']')
        assert 'ACCEPT' in out
        
        encoding = 'utf-8'
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=4', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMIClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('12. [Executed RETRIEVE AS Server 4 output:', out, ']')
        assert 'RETRIEVE :No such file on any peer' in out
        
        
if __name__ == "__main__":
    unittest.main()
