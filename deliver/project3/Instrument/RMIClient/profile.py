#!/usr/bin/env python3
from __future__ import print_function

import json
import os
import subprocess
import sys
import sys
import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
from matplotlib import cm
from matplotlib.ticker import LinearLocator, FormatStrFormatter
#from subprocess import run
from timeit import timeit
import re
import unittest
from collections import defaultdict, namedtuple
import timeit
T_CONVERSION=100

def generate_data():
    req_list  = []
    res_list  = []
    time_list = []

    test_profile_single = """subprocess.call(['java', '-jar', '-DclientId=1','-DserverChoice=1','RMISuperPeerClient.jar', 'RECIEVE','will_it_work.txt'],stdout=subprocess.PIPE)"""
    test_profile_single_two = """subprocess.call(['java', '-jar', '-DclientId=1','-DserverChoice=1','RMISuperPeerClient.jar', 'EDIT','will_it_work.txt'],stdout=subprocess.PIPE)"""
    test_profile_single_three = """subprocess.call(['java', '-jar', '-DclientId=1','-DserverChoice=1','RMISuperPeerClient.jar', 'REFRESH','will_it_work.txt'],stdout=subprocess.PIPE)"""
        
    reps = 1
    for req in range(1, 5):
        for res in range(1, 5):
            print(req, res)
            time = timeit.timeit(stmt = test_profile_single
                                  + "\n" + test_profile_single_two 
                                  + "\n" + test_profile_single_three, setup = "import subprocess, os", number=25) * T_CONVERSION / reps
            req_list.append(req)
            res_list.append(res)
            time_list.append(time)

    return req_list, res_list, time_list

def plot(data):
    res, req, times = data

    resset = set(res)
    reqset = set(req)
    req2d, res2d = np.meshgrid(sorted(resset), sorted(reqset))
    
    Z = np.zeros(shape=(max(reqset)+1, max(resset)+1))
    for ind, (i, j) in enumerate(zip(req, res)):
        Z[i, j] = times[ind]
    Z = Z[min(reqset):, min(resset):]

    f = plt.figure()
    ax2 = plt.axes(projection='3d')
    ax2.set_title('ALL TO ALL Time vs. Thread Count')
    surf2 = ax2.plot_surface(req2d, res2d, Z, cmap=plt.cm.coolwarm, linewidth=0)
    ax2.set_xlabel("# Requester Threads")
    ax2.set_ylabel('# Resolver Threads')
    ax2.set_zlabel('Time')
    f.colorbar(surf2, shrink=0.5, aspect=5)
    plt.savefig("performance.png")
    plt.show()

class MyClass(object):
    pass

if __name__ == "__main__":
    data = generate_data()

    plot(data)