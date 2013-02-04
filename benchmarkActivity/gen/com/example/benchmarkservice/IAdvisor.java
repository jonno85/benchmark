/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\users\\F31999A\\workspace\\Benchmark\\benchmarkActivity\\aidl\\com\\example\\benchmarkservice\\IAdvisor.aidl
 */
package com.example.benchmarkservice;
public interface IAdvisor extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.example.benchmarkservice.IAdvisor
{
private static final java.lang.String DESCRIPTOR = "com.example.benchmarkservice.IAdvisor";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.example.benchmarkservice.IAdvisor interface,
 * generating a proxy if needed.
 */
public static com.example.benchmarkservice.IAdvisor asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.example.benchmarkservice.IAdvisor))) {
return ((com.example.benchmarkservice.IAdvisor)iin);
}
return new com.example.benchmarkservice.IAdvisor.Stub.Proxy(obj);
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
case TRANSACTION_advice:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.advice();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.example.benchmarkservice.IAdvisor
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
@Override public int advice() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_advice, _data, _reply, 0);
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
static final int TRANSACTION_advice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public int advice() throws android.os.RemoteException;
}
