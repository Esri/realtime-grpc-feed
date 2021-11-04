'''
blocking, asynchronous, flow-control 
This script implements a gRPC client in Python. 
Before sending data using this client, a gRPC feed needs to be created on Velocity. 
The schema of the feed (data types & order) should match exactly the schema of the data sent from this client. 
The gRPC endpoint header path used in this script will be available on the details page after the feed is created. 
'''
import grpc
import velocity_grpc_pb2
import velocity_grpc_pb2_grpc
from google.protobuf.any_pb2 import Any
from google.protobuf import wrappers_pb2 as wrappers
import logging
import time

logging.basicConfig(level=logging.DEBUG)

SCHEMA = ['Float64', 'Float64', 'String']
PATH_TO_CER = 'ISRG Root X1.pem'
SERVER_URL = 'a4iot-a4iotdev-c2.westus2.cloudapp.azure.com'
SERVER_HEARDER = 'a4iotdev.89bb713384f146f89d73af23b7722dab'

def data_collection():
    '''
    Read data and translate it to a 2D array 
    '''
    # create a 2D array that contains the data to be sent 
    # the order and data types should match the schema specified on Velocity
    df = [[39.29242438926388, -76.6666720609419, '9:17AM'],
    [35.29242438926388, -70.6666720609419, '1:38PM'],
    [39.160776580893554, -77.30070336032382, '8:57AM']]

    return df 

def wrap_data(df):
    '''
    Wrap data to ensure the data types match 100% the schema on Velocity
    '''
    df_copy = df.copy()
    for data in df_copy:
        for i in range(0, len(SCHEMA)):
            if SCHEMA[i] == 'Float64':
                data[i] = wrappers.FloatValue(value=data[i])
            elif SCHEMA[i] == 'String':
                data[i] = wrappers.StringValue(value=data[i])

    return df_copy

def data_to_feature(df):
    '''
    Build features for requests from data 
    '''
    features = []
    for data in df: 
        # create a message of Any message type 
        any = Any()
        # create a feature 
        feature = velocity_grpc_pb2.Feature()
        
        for attribute in data:
            # pack the attribute into the message 
            any.Pack(attribute)
            # append the message to the empty feature so we now have a feature to send 
            feature.attributes.append(any)

        features.append(feature)
    
    return features

def create_channel():

    # read the root certificate from file
    # the certificate can be obtained from https://letsencrypt.org/certificates/
    with open(PATH_TO_CER, 'rb') as f:
        trusted_certs = f.read()
    # create credentials for the channel based on the root certificate 
    creds = grpc.ssl_channel_credentials(root_certificates=trusted_certs)

    # create the channel using the gRPC endpoint URL
    channel = grpc.secure_channel(SERVER_URL, creds)

    return channel

def send_blocking(stub, metadata, features):
    '''
    Send blocking requests to Velocity
    Each request contains one single feature 
    '''
    # send the request containing the feature we created
    for feature in features:
        res = stub.send(request = velocity_grpc_pb2.Request(features=[feature]), metadata=metadata)
        # print out the response '
        logging.info(res)

def process_async_responses(future):
    '''
    This function is called whenever a response has been received for an asychronous request
    It logs out the result of the request 
    '''
    # log the result of an asynchronous request
    print(future.result())

def send_asynchronous(stub, metadata, features):
    '''
    Send asynchronous requests 
    Each request contains one feature
    '''
    futures = []
    # loop through all features
    for i in range(0, 100):
        for feature in features:
            logging.info(feature)
            # send each feature via an asynchronous request 
            # this is non-blocking 
            call_future = stub.send.future(request = velocity_grpc_pb2.Request(features=[feature]), metadata=metadata)

            call_future.add_done_callback(process_async_responses)

            futures.append(call_future)

    return futures 

def stream_blocking(stub, metadata, features):
    '''
    Send streaming, blocking requests 
    Each request contains one feature 
    '''
    req_list = [velocity_grpc_pb2.Request(features=[feature]) for feature in features]
    res = stub.stream(request_iterator=req_list.__iter__(), metadata=metadata)
    # print out the response '
    logging.info(res)

def main():
        data = data_collection()
        wrapped_data = wrap_data(data)
        features = data_to_feature(wrapped_data)
        
        # send the request via the stub 
        channel = create_channel()

        # specify the gRPC endpoint header path that can be found on the details page of the created feed on Velocity
        metadata = (('grpc-path', SERVER_HEARDER),)
 
        # create a stub with the channel
        stub = velocity_grpc_pb2_grpc.GrpcFeedStub(channel)

        # send the features to Velocity via blocking requests 
        # i.e. a request is sent after the response from the server is received for the previous request 
        #send_blocking(stub=stub, metadata=metadata,features=features)

        # send the features to Velocity via asynchronous requests 
        #futures = send_asynchronous(stub=stub, metadata=metadata,features=features)

        # stream the features to Velocity with one feature in each request
        stream_blocking(stub=stub, metadata=metadata, features=features)
        
        """ futures_completed = False 
        
        while futures_completed != True:
            futures_completed = True
            for future in futures:
                if future.done():
                    continue
                else: 
                    futures_completed = False
                    print("Requests still not completed. Sleeping for 10 seconds before resuming.")
                    time.sleep(10) """

if __name__ == '__main__':
    logging.basicConfig()
    main()
