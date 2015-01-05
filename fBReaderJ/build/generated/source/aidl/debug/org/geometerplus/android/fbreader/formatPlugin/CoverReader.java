/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\Steven\\AndroidStudioProjects\\FBReaderJ1\\fBReaderJ\\src\\main\\aidl\\org\\geometerplus\\android\\fbreader\\formatPlugin\\CoverReader.aidl
 */
package org.geometerplus.android.fbreader.formatPlugin;
public interface CoverReader extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.geometerplus.android.fbreader.formatPlugin.CoverReader
{
private static final java.lang.String DESCRIPTOR = "org.geometerplus.android.fbreader.formatPlugin.CoverReader";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.geometerplus.android.fbreader.formatPlugin.CoverReader interface,
 * generating a proxy if needed.
 */
public static org.geometerplus.android.fbreader.formatPlugin.CoverReader asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.geometerplus.android.fbreader.formatPlugin.CoverReader))) {
return ((org.geometerplus.android.fbreader.formatPlugin.CoverReader)iin);
}
return new org.geometerplus.android.fbreader.formatPlugin.CoverReader.Stub.Proxy(obj);
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
case TRANSACTION_readBitmap:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
android.graphics.Bitmap _result = this.readBitmap(_arg0, _arg1, _arg2);
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
private static class Proxy implements org.geometerplus.android.fbreader.formatPlugin.CoverReader
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
@Override public android.graphics.Bitmap readBitmap(java.lang.String path, int maxWidth, int maxHeight) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.graphics.Bitmap _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(path);
_data.writeInt(maxWidth);
_data.writeInt(maxHeight);
mRemote.transact(Stub.TRANSACTION_readBitmap, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = android.graphics.Bitmap.CREATOR.createFromParcel(_reply);
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
static final int TRANSACTION_readBitmap = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public android.graphics.Bitmap readBitmap(java.lang.String path, int maxWidth, int maxHeight) throws android.os.RemoteException;
}
