#-----------------------------------------------------------------------------------------------------------------------------------
#-----------------------------------------------------------------------------------------------------------------------------------
#-----------------------------------------------------------------------------------------------------------------------------------
import warnings
warnings.filterwarnings("ignore")
#------------------------------------------------------------
import pandas     as pd
try:
    pd.set_option('display.width'      , 320)
    pd.set_option('display.max_rows'   , 2  )
    pd.set_option('display.max_columns', 8  )
    pd.options.display.float_format = '{:.6f}'.format
except Exception as err:
    print(err)
#------------------------------------------------------------
import numpy      as np
#------------------------------------------------------------
from   flask      import Flask, render_template, jsonify, request, redirect,url_for
#------------------------------------------------------------
from   flask_cors import CORS, cross_origin
#------------------------------------------------------------
from   threading import Thread
from   datetime  import date, datetime
from   time      import sleep, time, monotonic
import os
import sys
import csv
import math
import queue
#-----------------------------------------------------------------------------------------------------------------------------------
#-----------------------------------------------------------------------------------------------------------------------------------
#-----------------------------------------------------------------------------------------------------------------------------------
#-----------------------------------------------------------------------------------------------------------------------------------
class Logger:
    filename = ""
    hFile    = None
    writer   = None
    def __init__(self, filename:str ):
        self.filename = filename 
        self.hFile = open( self.filename , 'a', newline="" )
        if not self.hFile is None:
            self.writer = csv.writer( self.hFile, lineterminator='\n')
            if self.hFile.tell() == 0:
                try:
                    self.writer.writerow(['DateTime','Tile','SSID','RSS','Power'])
                    self.hFile.flush()
                except Exception as err:
                    print(err)
        else:
            errMsg = "Invalid file"
            print( "File open error : " , errMsg )
            raise Exception( errMsg )
        
    def __del__(self):
        try:
            if not self.hFile is None:
                self.hFile.flush()
                os.fsync(self.hFile.fileno())
                self.hFile.close()
        except Exception as err:
            print(err)
        
    def log(self, data:list):
        try:
            if not self.hFile is None:
                self.writer.writerow( data )
                self.hFile.flush()
        except Exception as err:
            print(err)

#-----------------------------------------------------------------------------------------------------------------------------------
app         = None
app         = Flask( __name__ , static_url_path='' , static_folder='./static', template_folder='./templates' )
gApp        = None
keepRunning = True
filename    = "/app/data/rss_data.csv"
logger      = None
logQueue    = queue.Queue(0)
errQueue    = queue.Queue(0)
msgQueue    = queue.Queue(0)
cwd         = os.getcwd()
_destFile   = ""
try:
    _destFile = filename
    logger      = Logger(filename)
except:
    try:
        _destFile = "./Data/rss_data.csv"
        logger      = Logger("./Data/rss_data.csv")
    except:
        _destFile = "../Data/rss_data.csv"
        logger      = Logger("../Data/rss_data.csv")

print("cwd : {} , logging uses {}".format(cwd,_destFile))

tile        = ""
A_Count     = 0
B_Count     = 0
C_Count     = 0
J_Count     = 0
#-----------------------------------------------------------------------------------------------------------------------------------
try:
    CORS( app , origins=['http://localhost:8686','http://127.0.0.1:8686','http://192.168.0.124:8686'] )
except Exception as err:
    print( err )
#-----------------------------------------------------------------------------------------------------------------------------------
@app.route("/", methods = ['GET'])
@cross_origin(supports_credentials=True)
def index():       
    try:
        if request.method == 'GET':
            return render_template('index.html')
    except Exception as err:
        return { "error" : "{}".format(err) }
       
#-----------------------------------------------------------------------------------------------------------------------------------
@app.route("/api/scan", methods = ['POST'])
@cross_origin(supports_credentials=True)
def scan():
    global logQueue
    global errQueue
    global msgQueue
    global tile        
    global A_Count 
    global B_Count 
    global C_Count    
    global J_Count     
    try:
        if request.method == 'POST':
            message = "Error"
            try:
                if 'tile' in request.json and 'ssid' in request.json and 'rss' in request.json and 'power' in request.json:
                    try:
                        _log = [ datetime.now().strftime("%H:%M:%S.%f") , request.json['tile'] , request.json['ssid'] , request.json['rss'] , request.json['power'] ]
                        logQueue.put_nowait( _log )

                        try:
                            if request.json['tile'] != tile:
                                tile    = request.json['tile']
                                A_Count = 0
                                B_Count = 0
                                C_Count = 0
                                J_Count = 0
                            if '69xXAnonymousXx69-1' in request.json['ssid']:
                                A_Count += 1
                                message = "SSID = {} => Tile = {} : A_Count = {} , B_Count = {} , C_Count = {} , J_Count = {}".format( str(request.json['ssid']).ljust(19) , request.json['tile'] , A_Count , B_Count , C_Count , J_Count )
                                msgQueue.put_nowait( message )
                            elif '69xXAnonymousXx69-2' in request.json['ssid']:
                                B_Count += 1
                                message= "SSID = {} => Tile = {} : A_Count = {} , B_Count = {} , C_Count = {} , J_Count = {}".format( str(request.json['ssid']).ljust(19) , request.json['tile'] , A_Count , B_Count , C_Count , J_Count )
                                msgQueue.put_nowait( message)
                            elif '69xXAnonymousXx69-3' in request.json['ssid']:
                                C_Count += 1
                                message = "SSID = {} => Tile = {} : A_Count = {} , B_Count = {} , C_Count = {} , J_Count = {}".format( str(request.json['ssid']).ljust(19) , request.json['tile'] , A_Count , B_Count , C_Count , J_Count )
                                msgQueue.put_nowait( message )
                            elif 'JUb4oa3ahq'          in request.json['ssid']:
                                J_Count += 1
                                message = "SSID = {} => Tile = {} : A_Count = {} , B_Count = {} , C_Count = {} , J_Count = {}".format( str(request.json['ssid']).ljust(19) , request.json['tile'] , A_Count , B_Count , C_Count , J_Count )
                                msgQueue.put_nowait( message )

                        except Exception as err:
                            message = "(1) Error {}".format( err )
                            errQueue.put_nowait( message )

                    except Exception as err:
                        message = "(2) Error {}".format( err )
                        errQueue.put_nowait( message )

            except Exception as err:
                message = "(3) Error {}".format( err )
                errQueue.put_nowait( message )

            return { 'POST http://localhost:8686/api/scan' : message }
        
    except Exception as err:
        message = "(4) Error {}".format( err )
        errQueue.put_nowait( message )
        return { "status" : "error" }
    
#-----------------------------------------------------------------------------------------------------------------------------------
def startListening():
    global keepRunning
    global msgQueue
    while( keepRunning ):
        try:
            _size = msgQueue.qsize()
            if _size > 0:
                items = []
                for i in range(_size):
                    try:
                        items.append( msgQueue.get_nowait() )
                    except Exception as err:
                        print( "\n\033[91mstartListening : {}\033[0m\n".format(err) )

                for item in items:
                    try:
                        print("\n\033[94mMessage received : {}\033[0m\n".format(item) , end="" )
                    except Exception as err:
                        print( "\n\033[91mstartListening : {}\033[0m\n".format(err) )
            sleep( 0.00001 )
        except Exception as err:
            print( "\n\033[91mstartListening : {}\033[0m\n".format(err) )
#-----------------------------------------------------------------------------------------------------------------------------------
#-----------------------------------------------------------------------------------------------------------------------------------
def debug():
    global keepRunning
    global errQueue
    while( keepRunning ):
        try:
            _size = errQueue.qsize()
            if _size > 0:
                items = []
                for i in range(_size):
                    try:
                        items.append( errQueue.get_nowait() )
                    except Exception as err:
                        print( "\n\033[91mdebug : {}\033[0m\n".format(err) )

                for item in items:
                    try:
                        print("\n\033[91m{}\033[0m\n".format(item) , end="" )
                    except Exception as err:
                        print( "\n\033[91mdebug : {}\033[0m\n".format(err) )
            sleep( 0.00001 )
        except Exception as err:
            print( "\n\033[91mdebug : {}\033[0m\n".format(err) )
#-----------------------------------------------------------------------------------------------------------------------------------   
def updateDatabase():
    global logger
    global keepRunning
    global logQueue
    while( keepRunning ):
        try:
            _size = logQueue.qsize()
            if _size > 0:
                items = []
                for i in range(_size):
                    try:
                        items.append( logQueue.get_nowait() )
                    except Exception as err:
                        print( "\n\033[91mupdateDatabase : {}\033[0m\n".format(err) , end="" )

                for item in items:
                    try:
                        logger.log( item )
                        print("\n\033[92mMessage logged : {}\033[0m\n".format(item) )
                    except Exception as err:
                        print( "\n\033[91mupdateDatabase : {}\033[0m\n".format(err) )
            sleep( 0.00001 )
        except Exception as err:
            print( "\n\033[91mupdateDatabase : {}\033[0m\n".format(err) )
#-----------------------------------------------------------------------------------------------------------------------------------
def Quit():
    print("Quit")

#-----------------------------------------------------------------------------------------------------------------------------------
if __name__ == "__main__":
    t0 = None
    t1 = None
    t2 = None
    try:
        try:
            t0 = Thread( target = updateDatabase )
            t0.daemon = True
        except Exception as err:
            print("threads creation : ",err)

        try:
            t1 = Thread( target = startListening )
            t1.daemon = True
        except Exception as err:
            print("threads creation : ",err)

        try:
            t2 = Thread( target = debug          )
            t2.daemon = True
        except Exception as err:
            print("threads creation : ",err)

        try:
            t0.start()
            t1.start()
            t2.start()
        except Exception as err:
            print("threads creation : ",err)

        app.run( host="0.0.0.0" , port=int("8686") , debug=True )
        keepRunning = False

        try:
            t0.join( timeout=0.5 )
            t0 = None
        except Exception as err:
            print("t0",err)
        try:
            t1.join( timeout=0.5 )
            t1 = None
        except Exception as err:
            print("t1",err)
        try:
            t2.join( timeout=0.5 )
            t2 = None
        except Exception as err:
            print("t2",err)
        
    except Exception as err:
        print("__main__ : ",err)
        keepRunning = False
        try:
            t0.join( timeout=0.5 )
            t0 = None
        except Exception as err:
            print("t0",err)
        try:
            t1.join( timeout=0.5 )
            t1 = None
        except Exception as err:
            print("t1",err)
        try:
            t2.join( timeout=0.5 )
            t2 = None
        except Exception as err:
            print("t2",err)

        try:
            sys.exit(130)
        except SystemExit:
            os._exit(130)

#-----------------------------------------------------------------------------------------------------------------------------------
# pip3 install -r requirements.txt --ignore-installed
"""
pyinstaller --noconfirm                           \
            --onefile                             \
            --add-data "templates;templates"      \
            --add-data "static;static"            \
            --hidden-import gunicorn.glogging     \
            --hidden-import gunicorn.workers.sync \
            ./main.py

pyinstaller --noconfirm --onefile --add-data="templates:templates" --add-data="static:static" main.py
"""
# ./main.exe
# row 7571