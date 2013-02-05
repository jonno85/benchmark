/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\users\\F31999A\\workspace\\Benchmark\\benchmarkPipeService\\aidl\\com\\example\\benchmarkservice\\IPipeService.aidl
 */
package com.example.benchmarkservice;
public interface IPipeService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.example.benchmarkservice.IPipeService
{
private static final java.lang.String DESCRIPTOR = "com.example.benchmarkservice.IPipeService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.example.benchmarkservice.IPipeService interface,
 * generating a proxy if needed.
 */
public static com.example.benchmarkservice.IPipeService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.example.benchmarkservice.IPipeService))) {
return ((com.example.benchmarkservice.IPipeService)iin);
}
return new com.example.benchmarkservice.IPipeService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getSourcePipe:
{
data.enforceInterface(DESCRIPTOR);
com.example.benchmarkservice.PipeSourceChannel _result = this.getSourcePipe();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_run:
{
data.enforceInterface(DESCRIPTOR);
this.run();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.example.benchmarkservice.IPipeService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public com.example.benchmarkservice.PipeSourceChannel getSourcePipe() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.example.benchmarkservice.PipeSourceChannel _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSourcePipe, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = com.example.benchmarkservice.PipeSourceChannel.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void run() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_run, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_getSourcePipe = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_run = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public com.example.benchmarkservice.PipeSourceChannel getSourcePipe() throws android.os.RemoteException;
public void run() throws android.os.RemoteException;
}
