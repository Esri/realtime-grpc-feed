from __future__ import print_function

import logging
from google.protobuf.any_pb2 import Any

import grpc
import velocity_grpc_pb2
import velocity_grpc_pb2_grpc
from google.protobuf import wrappers_pb2 as wrappers


def send_velocity_features(stub):

    # create a list that contains only one event to send to Velocity
    # make sure the attributes are wrapped to match the data types listed in the schema on Velocity feed
    events = [
        wrappers.Int64Value(value=37592424978605884),
        wrappers.Int64Value(value=-9276911615610153),
        wrappers.StringValue(value='test_loc_0')
        ]
    
    # create a message of Any message type 
    any = Any()
    # create a feature 
    test_feature = velocity_grpc_pb2.Feature()

    # loop through the list of events 
    for event in events:
        # pack the event 
        any.Pack(event)
        # append the event to the empty feature so we now have a feature to send 
        test_feature.attributes.append(any)

    # specify the gRPC endpoint header path that can be found on the details page of the created feed on Velocity
    metadata = (('grpc-path', 'a4iotdev.978868e6b2254f579fa95baa35fbc431'),)

    # create a request containing the feature we created
    res = stub.send(request = velocity_grpc_pb2.Request(features=[test_feature]), metadata=metadata)

    return res

def run():

    # read the root certificate from file
    with open('ISRG Root X1.pem', 'rb') as f:
        trusted_certs = f.read()
    # create credentials for the channel based on the root certificate 
    creds = grpc.ssl_channel_credentials(root_certificates=trusted_certs)

    # create the channel using the gRPC endpoint URL
    with grpc.secure_channel('a4iot-a4iotdev-c2.westus2.cloudapp.azure.com', creds) as channel:
        # create a stub using the channel
        stub = velocity_grpc_pb2_grpc.GrpcFeedStub(channel)
        # send the request via the stub 
        response = send_velocity_features(stub)
        # print out the response 
        print(response)


if __name__ == '__main__':
    logging.basicConfig()
    run()
