'''
/* Copyright 2021 Esri
 *
 * Licensed under the Apache License Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'''

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

logging.basicConfig(level=logging.DEBUG)

# the schema of the data sent to Velocity 
SCHEMA = ['Float32', 'Float32', 'String', 'Boolean', 'Int32', 'Date']
# path to the certificate file 
PATH_TO_CER = 'C:\\Users\\keng4472\\Downloads\\isrgrootx1.pem'
# URL and header provided on the feed details page on Velocity 
SERVER_URL = 'velocitydemo.arcgis.com:443'
SERVER_HEARDER = 'bc1qjuyagnrebxvh.90ae5461da044d60a9856ac2f17fa89f'

def data_collection():
    '''
    Provide data as a 2D array 
    '''
    # create a 2D array that contains the data to be sent 
    # the order and data types should match the schema specified on Velocity
    df = [[39.29242438926388, -76.6666720609419, '9:17AM', False, 4, "1636384539"],
    [35.29242438926388, -70.6666720609419, '1:38PM', False, 2, "1636384539"],
    [39.160776580893554, -77.30070336032382, '8:57AM', True, 3, "1636384539"]]

    return df 

def wrap_data(df):
    '''
    Wrap data to ensure the data types match the schema on Velocity
    '''
    # create a copy of the list 
    df_copy = df.copy()
    # loop through the list and wrap each entry based on its data type 
    for data in df_copy:
        for i in range(0, len(SCHEMA)):
            if SCHEMA[i] == 'Float64' or SCHEMA[i] == 'Float32':
                data[i] = wrappers.FloatValue(value=data[i])
            elif SCHEMA[i] == 'String':
                data[i] = wrappers.StringValue(value=data[i])
            elif SCHEMA[i] == 'Boolean':
                data[i] = wrappers.BoolValue(value=data[i])
            elif SCHEMA[i] == 'Int32':
                data[i] = wrappers.Int32Value(value=data[i])
            elif SCHEMA[i] == 'Int64':
                data[i] = wrappers.Int64Value(value=data[i])
            elif SCHEMA[i] == 'Date':
                data[i] = wrappers.StringValue(value=data[i])
            else: 
                logging.info('Incorrect data type entered')


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
        
        # loop through the attributes of each data point 
        for attribute in data:
            # pack the attribute into the message 
            any.Pack(attribute)
            # append the message to the empty feature so we now have a feature to send 
            feature.attributes.append(any)

        features.append(feature)
    
    return features

def create_channel():
    '''
    Create the channel to send data 
    '''
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
        res = stub.Send(request = velocity_grpc_pb2.Request(features=[feature]), metadata=metadata)
        # print out the response '
        logging.info(res)

def main():

        # obtain the data 
        data = data_collection()
        # wrap data so the data types match the schema 
        wrapped_data = wrap_data(data)
        # convert data points to features 
        features = data_to_feature(wrapped_data)
        
        # create the channel to send data 
        channel = create_channel()

        # specify the gRPC endpoint header path that can be found on the details page of the created feed on Velocity
        metadata = (('grpc-path', SERVER_HEARDER),)
 
        # create a stub with the channel
        stub = velocity_grpc_pb2_grpc.GrpcFeedStub(channel)

        # send the features to Velocity as blocking requests 
        # i.e. a request is sent after the response from the server is received for the previous request 
        send_blocking(stub=stub, metadata=metadata,features=features)

if __name__ == '__main__':
    logging.basicConfig()
    main()
