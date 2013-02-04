/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\users\\F31999A\\workspace\\Benchmark\\benchmarkActivity\\aidl\\com\\example\\benchmarkservice\\IBenchMarkService.aidl
 */
package com.example.benchmarkservice;
public interface IBenchMarkService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.example.benchmarkservice.IBenchMarkService
{
private static final java.lang.String DESCRIPTOR = "com.example.benchmarkservice.IBenchMarkService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.example.benchmarkservice.IBenchMarkService interface,
 * generating a proxy if needed.
 */
public static com.example.benchmarkservice.IBenchMarkService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.example.benchmarkservice.IBenchMarkService))) {
return ((com.example.benchmarkservice.IBenchMarkService)iin);
}
return new com.example.benchmarkservice.IBenchMarkService.Stub.Proxy(obj);
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
case TRANSACTION_startRunning:
{
data.enforceInterface(DESCRIPTOR);
this.startRunning();
reply.writeNoException();
return true;
}
case TRANSACTION_stopRunning:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.stopRunning();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_setBurstSize:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.setBurstSize(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setReturnCallback:
{
data.enforceInterface(DESCRIPTOR);
com.example.benchmarkservice.IAdvisor _arg0;
_arg0 = com.example.benchmarkservice.IAdvisor.Stub.asInterface(data.readStrongBinder());
this.setReturnCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getNPackets:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getNPackets();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.example.benchmarkservice.IBenchMarkService
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
@Override public void startRunning() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_startRunning, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public long stopRunning() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stopRunning, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setBurstSize(int size) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(size);
mRemote.transact(Stub.TRANSACTION_setBurstSize, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void setReturnCallback(com.example.benchmarkservice.IAdvisor advisor) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((advisor!=null))?(advisor.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_setReturnCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int getNPackets() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getNPackets, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_startRunning = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_stopRunning = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_setBurstSize = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_setReturnCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getNPackets = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
}
public void startRunning() throws android.os.RemoteException;
public long stopRunning() throws android.os.RemoteException;
public void setBurstSize(int size) throws android.os.RemoteException;
public void setReturnCallback(com.example.benchmarkservice.IAdvisor advisor) throws android.os.RemoteException;
public int getNPackets() throws android.os.RemoteException;
}
