#!/usr/bin/env python3
import grpc
import grpc_feed_pb2
import grpc_feed_pb2_grpc
from google.protobuf import wrappers_pb2 as wrappers
from google.protobuf import any_pb2
import argparse

parser = argparse.ArgumentParser(description='Send features using python velocity client.')
parser.add_argument('--stream', action='store_true', help='Enables stream features for higher velocity')
parser.add_argument('--token', dest='token', help='Bearer token sent with requests')
parser.add_argument('url', help='endpoint provided by velocity')
parser.add_argument('path', help='grpc path provided by velocity')
args = parser.parse_args()

values = vars(args)

isStream = values.get('stream')
token = values.get('token')
grpc_path = values.get('path')
url = values.get('url')

############# parsed data ##############
elements = [
    wrappers.StringValue(value='iss'),
    wrappers.Int32Value(value=25544),
    wrappers.Int64Value(value=1588093054050),
    wrappers.DoubleValue(value=34.265521039000078),
    wrappers.DoubleValue(value=0.585739043999979),
    wrappers.StringValue(value='kilometers')
]

feature = grpc_feed_pb2.Feature()
target = any_pb2.Any()

for element in elements:
    target.Pack(element)
    feature.attributes.append(target)


class RequestIterator:
    request = grpc_feed_pb2.Request(features=[feature])

    def __iter__(self):
        return self

    def __next__(self):
        return self.request

it = RequestIterator()
creds = ''
channel = ''
creds = grpc.ssl_channel_credentials()

if token:
    creds = grpc.composite_channel_credentials(creds, grpc.access_token_call_credentials(token))

# channel - tells the client where the req should go
channel = grpc.secure_channel(url, creds)

# stub
# https://stackoverflow.com/questions/56425327/protobuf-and-python-how-to-add-messages-to-repeatable-any-field?rq=1
stub = grpc_feed_pb2_grpc.GrpcFeedStub(channel)

if __name__ == "__main__":
    metadata = (('grpc-path', grpc_path),)

    if isStream:
        print('streaming requests...')
        response = stub.stream(request_iterator=it, metadata=metadata)
    else:
        request = grpc_feed_pb2.Request(features=[feature])
        response = stub.send(request, metadata=metadata)
        print(response)

