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
from profile import MyClass
import shutil
import json
        

class TestPeerToPeerMethods(unittest.TestCase):
    def setupClass(self):
       unittest.TestCase.setUp(self)
       self.__class__.myclass = MyClass()
    
    ClassIsSetup = False
    new_files = []
    def setUp(self):
        self.new_files = self.new_files
        if not self.ClassIsSetup:
            print("Initializing testing environment")
            # run the real setup
            self.new_files = []
            self.new_files_dir_appended = []
            length = 10000
            letters = string.ascii_lowercase
            print('setup')
            def get_parent_dir(directory):
                import os
                return os.path.dirname(directory)
            for dirs in os.walk((get_parent_dir(get_parent_dir(os.getcwd()))) + "/Gnutella-1/RMIServer1/files"):
                d = dirs[0]
                print(d)
                for i in range(1):
                    filename = str(uuid.uuid4()) + ".txt"
                    print(filename)
                    self.new_files.append(filename)
                    self.new_files_dir_appended.append(os.path.join(d, filename))
                    size = random.randint(1, 100)
                    with open(os.path.join(d, filename), "w") as f:
                        f.write(''.join(random.choice(letters) for i in range(length)))
            print("Added NEW TEST FILES ", self.new_files)
            self.setupClass()
            # remember that it was setup already
            self.__class__.ClassIsSetup = True
            self.__class__.new_files = self.new_files
    
    
    def test_a_new_file_gets_added_single_gnutella_from_pa1(self):
        def get_parent_dir(directory):
            import os
            return os.path.dirname(directory)
        
        encoding = 'utf-8'
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=4', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('1. [Executed RETRIEVE AS Gnutella 1, Server 4 output:', out, ']')
        #assert 'RETRIEVE :No such file on any peer' in out
        self.new_files_dir_appended.append((get_parent_dir(get_parent_dir(os.getcwd()))) + "/Gnutella-1/RMIServer4/download"
                                            + self.new_files[0])
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=3', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('2. [Executed RETRIEVE AS Gnutella 1, Server 3 output:', out, ']')
        self.new_files_dir_appended.append(os.path.join((get_parent_dir(get_parent_dir(os.getcwd()))) 
                                           + "/Gnutella-1/RMIServer3/download"
                                            ,self.new_files[0]))
        #assert 'No such file on any peer' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=2', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'SEARCH',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('3. [Executed SEARCH AS Gnutella 1, Server 2 output:', out, ']')
        assert 'SEARCH :localhost:2345' in out
        
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=1', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'REGISTRY',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('5. [Executed REGISTER AS Gnutella 1, Server 1 output:', out, ' for file ', self.new_files[0], ']')
        assert 'ACCEPT' in out
                
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=2', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'SEARCH',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('6. [Executed SEARCH AS Gnutella 1, Server 2 output:', out, ']') 
        assert 'SEARCH :localhost:2345' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=3', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        
        print('7. [Executed RETRIEVE AS Gnutella 1, Server 3 output:', out, ']') 
        
        assert 'File Already Latest' in out
        self.new_files_dir_appended.append(os.path.join((get_parent_dir(get_parent_dir(os.getcwd()))) 
                                           + "/Gnutella-1/RMIServer3/download"
                                            ,self.new_files[0]))
        
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=2',
                                               '-DserverChoice=3', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'RETRIEVE',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        
        print('10. [Executed SEARCH AS Gnutella 2, Server 3 output:', out, ']') 
        assert 'FileRepositoryFile '+ self.new_files[0]+ ' Downloaded at localhost:4545' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=1', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'DEREGISTER',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('11. [EXECUTED REGISTRY AS Gnutella 1, Server 1 output:', out, ']')
        assert 'ACCEPT' in out
        
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=2', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'DEREGISTER',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('12. [EXECUTED DEREGISTRY AS Gnutella 1, Server 2 output:', out, ']')
        assert 'ACCEPT' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=3', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'DEREGISTER',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('13. [EXECUTED DEREGISTRY AS Gnutella 1, Server 3 output:', out, ']')
        assert 'ACCEPT' in out
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=4', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'DEREGISTER',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('14. [EXECUTED DEREGISTRY AS Gnutella 1, Server 4 output:', out, ']')
        assert 'ACCEPT' in out
        
        
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=1', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'DEREGISTER',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        print('15. [EXECUTED DEREGISTRY AS Gnutella 1, Server 1 output:', out, ']')
        assert 'ACCEPT' in out
        
        encoding = 'utf-8'
    
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
        for dirs in os.walk((get_parent_dir(get_parent_dir(os.getcwd()))) + "/Gnutella-1/RMIServer1/file_metadata"):
            d = dirs[0]
            file_a_location = os.path.join(d, self.new_files[0] + ".json")
        
        file_b_location = None
        for dirs in os.walk((get_parent_dir(get_parent_dir(os.getcwd()))) + "/Gnutella-1/RMIServer3/file_metadata"):
            d = dirs[0]
            file_b_location = os.path.join(d, self.new_files[0] + ".json")
        
        
        file_c_location = None
        for dirs in os.walk((get_parent_dir(get_parent_dir(os.getcwd()))) + "/Gnutella-1/RMIServer4/file_metadata"):
            d = dirs[0]
            file_c_location = os.path.join(d, self.new_files[0] + ".json")

        # returns JSON object as
        # a dictionary
        origin_server_metadata = json.load(open(file_a_location))
        leaf_server_metadata_one = json.load(open(file_b_location))
        leaf_server_metadata_two = json.load(open(file_c_location))
        
         
        # Iterating through the json
        # list
        
        assert origin_server_metadata['status'] == "VALID"
        assert origin_server_metadata['isMasterClient'] == True
        assert origin_server_metadata['version'] == 0
        assert leaf_server_metadata_one['status'] == "VALID"
        assert leaf_server_metadata_one['isMasterClient'] == False
        assert leaf_server_metadata_two['status'] == "VALID"
        assert leaf_server_metadata_one['isMasterClient'] == False
        
        encoding = 'utf-8'
        subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                               '-DserverChoice=1', 
                                               os.path.join(os.path.dirname(__file__),
                                                             'RMISuperPeerClient.jar'), 'EDIT',
                                                self.new_files[0]],stdout=subprocess.PIPE)
        out, err = subprocess_output.communicate()
        out = str(out.decode(encoding))
        
        origin_server_metadata = json.load(open(file_a_location))
        leaf_server_metadata_one = json.load(open(file_b_location))
        leaf_server_metadata_two = json.load(open(file_c_location))
        
        assert origin_server_metadata['status'] == "VALID"
        assert origin_server_metadata['isMasterClient'] == True
        assert origin_server_metadata['version'] == 1
        print(leaf_server_metadata_one['status'])
        assert leaf_server_metadata_one['status'] == "FILE OUT OF DATE"
        assert leaf_server_metadata_one['isMasterClient'] == False
        assert leaf_server_metadata_two['status'] == "FILE OUT OF DATE"
        assert leaf_server_metadata_one['isMasterClient'] == False
        
        
        print('1. [Executed EDIT MESSAGE', self.new_files[0], ' On GNUTELLA-1, RMIServer1 output:', out, ']')

    def test_profile(self):
            env = {
                **os.environ,
                "PUSH_BASED_CONSISTENCY": "TRUE",
                "TTR": "450",
            }
            encoding = 'utf-8'
            subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                                   '-DserverChoice=1', 
                                                   os.path.join(os.path.dirname(__file__),
                                                                 'RMISuperPeerClient.jar'), 'EDIT',
                                                    self.new_files[0]],stdout=subprocess.PIPE, env=env)
            out, err = subprocess_output.communicate()
            out = str(out.decode(encoding))
            
            for i in range (30):
                encoding = 'utf-8'
                subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                                       '-DserverChoice=1', 
                                                       os.path.join(os.path.dirname(__file__),
                                                                     'RMISuperPeerClient.jar'), 'EDIT',
                                                        self.new_files[0]],stdout=subprocess.PIPE, env=env)
                out, err = subprocess_output.communicate()
                out = str(out.decode(encoding))
                subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                                   '-DserverChoice=4', 
                                                   os.path.join(os.path.dirname(__file__),
                                                                 'RMISuperPeerClient.jar'), 'REFRESH',
                                                    self.new_files[0]],stdout=subprocess.PIPE,env=env)
            
                
                out, err = subprocess_output.communicate()
                out = str(out.decode(encoding))
                subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                                   '-DserverChoice=3', 
                                                   os.path.join(os.path.dirname(__file__),
                                                                 'RMISuperPeerClient.jar'), 'REFRESH',
                                                    self.new_files[0]],stdout=subprocess.PIPE,env=env)
            
            
            subprocess_output = subprocess.Popen(['java', '-jar', '-DclientId=1',
                                                   '-DserverChoice=4', 
                                                   os.path.join(os.path.dirname(__file__),
                                                                 'RMISuperPeerClient.jar'), 'REFRESH',
                                                    self.new_files[0]],stdout=subprocess.PIPE,env=env)
            

def suite():
    suite = unittest.TestSuite()
    if os.getenv("PROFILE") == "TRUE":
        suite.addTest(TestPeerToPeerMethods('test_profile'))
    else:
        suite.addTest(TestPeerToPeerMethods('test_a_new_file_gets_added_single_gnutella_from_pa1'))
    return suite

if __name__ == '__main__':
    runner = unittest.TextTestRunner()
    runner.run(suite())