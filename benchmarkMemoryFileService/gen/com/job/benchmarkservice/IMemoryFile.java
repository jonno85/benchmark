/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\users\\F31999A\\workspace\\Benchmark\\benchmarkMemoryFileService\\aidl\\com\\job\\benchmarkservice\\IMemoryFile.aidl
 */
package com.job.benchmarkservice;
public interface IMemoryFile extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.job.benchmarkservice.IMemoryFile
{
private static final java.lang.String DESCRIPTOR = "com.job.benchmarkservice.IMemoryFile";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.job.benchmarkservice.IMemoryFile interface,
 * generating a proxy if needed.
 */
public static com.job.benchmarkservice.IMemoryFile asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.job.benchmarkservice.IMemoryFile))) {
return ((com.job.benchmarkservice.IMemoryFile)iin);
}
return new com.job.benchmarkservice.IMemoryFile.Stub.Proxy(obj);
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
case TRANSACTION_getFileDescriptor:
{
data.enforceInterface(DESCRIPTOR);
com.job.benchmarkservice.SerFD _result = this.getFileDescriptor();
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
case TRANSACTION_getInputStream:
{
data.enforceInterface(DESCRIPTOR);
com.job.benchmarkservice.SerInpStr _result = this.getInputStream();
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
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.job.benchmarkservice.IMemoryFile
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
@Override public com.job.benchmarkservice.SerFD getFileDescriptor() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.job.benchmarkservice.SerFD _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getFileDescriptor, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = com.job.benchmarkservice.SerFD.CREATOR.createFromParcel(_reply);
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
@Override public com.job.benchmarkservice.SerInpStr getInputStream() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.job.benchmarkservice.SerInpStr _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getInputStream, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = com.job.benchmarkservice.SerInpStr.CREATOR.createFromParcel(_reply);
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
}
static final int TRANSACTION_getFileDescriptor = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getInputStream = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public com.job.benchmarkservice.SerFD getFileDescriptor() throws android.os.RemoteException;
public com.job.benchmarkservice.SerInpStr getInputStream() throws android.os.RemoteException;
}
