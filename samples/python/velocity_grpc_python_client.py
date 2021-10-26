'''
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

def send_velocity_features(stub):
    '''
    Define a function to create a request and send it to the gRPC feed created on Velocity
    This request contains one feature with three attributes 
    '''
    # create a list that contains the attributes of the event 
    # make sure the attributes are wrapped to match the data types listed in the schema on Velocity feed
    attributes = [
        wrappers.FloatValue(value=37.592424978605884),
        wrappers.FloatValue(value=-92.76911615610153),
        wrappers.StringValue(value='test_loc_0')
        ]
    
    # create a message of Any message type 
    any = Any()
    # create a feature 
    test_feature = velocity_grpc_pb2.Feature()

    # loop through the list of attributes
    for attri in attributes:
        # pack the attribute into the message 
        any.Pack(attri)
        # append the message to the empty feature so we now have a feature to send 
        test_feature.attributes.append(any)

    # specify the gRPC endpoint header path that can be found on the details page of the created feed on Velocity
    metadata = (('grpc-path', 'a4iotdev.978868e6b2254f579fa95baa35fbc431'),)

    # send the request containing the feature we created
    res = stub.send(request = velocity_grpc_pb2.Request(features=[test_feature]), metadata=metadata)

    return res

def run():

    # read the root certificate from file
    # the certificate can be obtained from https://letsencrypt.org/certificates/
    with open('ISRG Root X1.pem', 'rb') as f:
        trusted_certs = f.read()
    # create credentials for the channel based on the root certificate 
    creds = grpc.ssl_channel_credentials(root_certificates=trusted_certs)

    # create the channel using the gRPC endpoint URL
    with grpc.secure_channel('a4iot-a4iotdev-c2.westus2.cloudapp.azure.com', creds) as channel:
        # create a stub with the channel
        stub = velocity_grpc_pb2_grpc.GrpcFeedStub(channel)
        # send the request via the stub 
        response = send_velocity_features(stub)
        # print out the response 
        logging.info(response)


if __name__ == '__main__':
    logging.basicConfig()
    run()
