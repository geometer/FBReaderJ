/*
 * Copyright (C) 2004-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

#ifndef __SHARED_PTR_H__
#define __SHARED_PTR_H__

template<class T> class shared_ptr_storage {
	private:
		unsigned int myCounter;
		unsigned int myWeakCounter;
		T* myPointer;

	public:
		shared_ptr_storage(T *pointer);
		~shared_ptr_storage();

		T* pointer() const;
		T& content() const;

		void addReference();
		void removeReference();
		void addWeakReference();
		void removeWeakReference();
		unsigned int counter() const;
};

template<class T> class weak_ptr;

template<class T> class shared_ptr {
	friend class weak_ptr<T>;

	private:
		shared_ptr_storage<T> *myStorage;

		shared_ptr_storage<T> *newStorage(T *t);
		void attachStorage(shared_ptr_storage<T> *storage);
		void detachStorage();

	public:
		shared_ptr();
		shared_ptr(T *t);
		shared_ptr(const shared_ptr<T> &t);
		shared_ptr(const weak_ptr<T> &t);
		~shared_ptr();

		const shared_ptr<T> &operator = (T *t);
		const shared_ptr<T> &operator = (const shared_ptr<T> &t);
		const shared_ptr<T> &operator = (const weak_ptr<T> &t);

		T* operator -> () const;
		T& operator * () const;
		bool isNull() const;
		void reset();
		bool operator == (const weak_ptr<T> &t) const;
		bool operator != (const weak_ptr<T> &t) const;
		bool operator < (const weak_ptr<T> &t) const;
		bool operator > (const weak_ptr<T> &t) const;
		bool operator <= (const weak_ptr<T> &t) const;
		bool operator >= (const weak_ptr<T> &t) const;
		bool operator == (const shared_ptr<T> &t) const;
		bool operator != (const shared_ptr<T> &t) const;
		bool operator < (const shared_ptr<T> &t) const;
		bool operator > (const shared_ptr<T> &t) const;
		bool operator <= (const shared_ptr<T> &t) const;
		bool operator >= (const shared_ptr<T> &t) const;
};

template<class T> class weak_ptr {
	friend class shared_ptr<T>;
	private:
		shared_ptr_storage<T> *myStorage;

		void attachStorage(shared_ptr_storage<T> *storage);
		void detachStorage();

	public:
		weak_ptr();
		weak_ptr(const shared_ptr<T> &t);
		weak_ptr(const weak_ptr<T> &t);
		~weak_ptr();

		const weak_ptr<T> &operator = (const weak_ptr<T> &t);
		const weak_ptr<T> &operator = (const shared_ptr<T> &t);

		T* operator -> () const;
		T& operator * () const;
		bool isNull() const;
		void reset();

		bool operator == (const weak_ptr<T> &t) const;
		bool operator != (const weak_ptr<T> &t) const;
		bool operator < (const weak_ptr<T> &t) const;
		bool operator > (const weak_ptr<T> &t) const;
		bool operator <= (const weak_ptr<T> &t) const;
		bool operator >= (const weak_ptr<T> &t) const;
		bool operator == (const shared_ptr<T> &t) const;
		bool operator != (const shared_ptr<T> &t) const;
		bool operator < (const shared_ptr<T> &t) const;
		bool operator > (const shared_ptr<T> &t) const;
		bool operator <= (const shared_ptr<T> &t) const;
		bool operator >= (const shared_ptr<T> &t) const;
};

template<class T>
inline shared_ptr_storage<T>::shared_ptr_storage(T *pointer) {
	myPointer = pointer;
	myCounter = 0;
	myWeakCounter = 0;
}
template<class T>
inline shared_ptr_storage<T>::~shared_ptr_storage() {
}
template<class T>
inline T* shared_ptr_storage<T>::pointer() const {
	return myPointer;
}
template<class T>
inline T& shared_ptr_storage<T>::content() const {
	return *myPointer;
}
template<class T>
inline void shared_ptr_storage<T>::addReference() {
	++myCounter;
}
template<class T>
inline void shared_ptr_storage<T>::removeReference() {
	--myCounter;
	if (myCounter == 0) {
		T* ptr = myPointer;
		myPointer = 0;
		delete ptr;
	}
}
template<class T>
inline void shared_ptr_storage<T>::addWeakReference() {
	++myWeakCounter;
}
template<class T>
inline void shared_ptr_storage<T>::removeWeakReference() {
	--myWeakCounter;
}
template<class T>
inline unsigned int shared_ptr_storage<T>::counter() const {
	return myCounter + myWeakCounter;
}

template<class T>
inline shared_ptr_storage<T> *shared_ptr<T>::newStorage(T *t) {
	return (t == 0) ? 0 : new shared_ptr_storage<T>(t);
}
template<class T>
inline void shared_ptr<T>::attachStorage(shared_ptr_storage<T> *storage) {
	myStorage = storage;
	if (myStorage != 0) {
		myStorage->addReference();
	}
}
template<class T>
inline void shared_ptr<T>::detachStorage() {
	if (myStorage != 0) {
		if (myStorage->counter() == 1) {
			myStorage->removeReference();
			delete myStorage;
		} else {
			myStorage->removeReference();
		}
	}
}

template<class T>
inline shared_ptr<T>::shared_ptr() {
	myStorage = 0;
}
template<class T>
inline shared_ptr<T>::shared_ptr(T *t) {
	attachStorage(newStorage(t));
}
template<class T>
inline shared_ptr<T>::shared_ptr(const shared_ptr<T> &t) {
	attachStorage(t.myStorage);
}
template<class T>
inline shared_ptr<T>::shared_ptr(const weak_ptr<T> &t) {
	if (!t.isNull()) {
		attachStorage(t.myStorage);
	} else {
		attachStorage(0);
	}
}
template<class T>
inline shared_ptr<T>::~shared_ptr() {
	detachStorage();
}
template<class T>
inline const shared_ptr<T> &shared_ptr<T>::operator = (T *t) {
	detachStorage();
	attachStorage(newStorage(t));
	return *this;
}
template<class T>
inline const shared_ptr<T> &shared_ptr<T>::operator = (const shared_ptr<T> &t) {
	if (&t != this) {
		const bool flag = !t.isNull();
		if (flag) {
			t.myStorage->addReference();
		}
		detachStorage();
		attachStorage(t.myStorage);
		if (flag) {
			t.myStorage->removeReference();
		}
	}
	return *this;
}
template<class T>
inline const shared_ptr<T> &shared_ptr<T>::operator = (const weak_ptr<T> &t) {
	if (!t.isNull()) {
		t.myStorage->addReference();
		detachStorage();
		attachStorage(t.myStorage);
		t.myStorage->removeReference();
	} else {
		detachStorage();
		attachStorage(0);
	}
	return *this;
}

template<class T>
inline T* shared_ptr<T>::operator -> () const {
	return (myStorage == 0) ? 0 : myStorage->pointer();
}
template<class T>
inline T& shared_ptr<T>::operator * () const {
	return myStorage->content();
}
template<class T>
inline bool shared_ptr<T>::isNull() const {
	return myStorage == 0;
}
template<class T>
inline void shared_ptr<T>::reset() {
	detachStorage();
	attachStorage(0);
}
template<class T>
inline bool shared_ptr<T>::operator == (const weak_ptr<T> &t) const {
	return operator -> () == t.operator -> ();
}
template<class T>
inline bool shared_ptr<T>::operator != (const weak_ptr<T> &t) const {
	return !operator == (t);
}
template<class T>
inline bool shared_ptr<T>::operator < (const weak_ptr<T> &t) const {
	return operator -> () < t.operator -> ();
}
template<class T>
inline bool shared_ptr<T>::operator > (const weak_ptr<T> &t) const {
	return t.operator < (*this);
}
template<class T>
inline bool shared_ptr<T>::operator <= (const weak_ptr<T> &t) const {
	return !t.operator < (*this);
}
template<class T>
inline bool shared_ptr<T>::operator >= (const weak_ptr<T> &t) const {
	return !operator < (t);
}
template<class T>
inline bool shared_ptr<T>::operator == (const shared_ptr<T> &t) const {
	return operator -> () == t.operator -> ();
}
template<class T>
inline bool shared_ptr<T>::operator != (const shared_ptr<T> &t) const {
	return !operator == (t);
}
template<class T>
inline bool shared_ptr<T>::operator < (const shared_ptr<T> &t) const {
	return operator -> () < t.operator -> ();
}
template<class T>
inline bool shared_ptr<T>::operator > (const shared_ptr<T> &t) const {
	return t.operator < (*this);
}
template<class T>
inline bool shared_ptr<T>::operator <= (const shared_ptr<T> &t) const {
	return !t.operator < (*this);
}
template<class T>
inline bool shared_ptr<T>::operator >= (const shared_ptr<T> &t) const {
	return !operator < (t);
}

template<class T>
inline void weak_ptr<T>::attachStorage(shared_ptr_storage<T> *storage) {
	myStorage = storage;
	if (myStorage != 0) {
		myStorage->addWeakReference();
	}
}
template<class T>
inline void weak_ptr<T>::detachStorage() {
	if (myStorage != 0) {
		myStorage->removeWeakReference();
		if (myStorage->counter() == 0) {
			delete myStorage;
		}
	}
}

template<class T>
inline weak_ptr<T>::weak_ptr() {
	myStorage = 0;
}
template<class T>
inline weak_ptr<T>::weak_ptr(const shared_ptr<T> &t) {
	attachStorage(t.myStorage);
}
template<class T>
inline weak_ptr<T>::weak_ptr(const weak_ptr<T> &t) {
	if (!t.isNull()) {
		attachStorage(t.myStorage);
	} else {
		attachStorage(0);
	}
}
template<class T>
inline weak_ptr<T>::~weak_ptr() {
	detachStorage();
}

template<class T>
inline const weak_ptr<T> &weak_ptr<T>::operator = (const weak_ptr<T> &t) {
	if (&t != this) {
		detachStorage();
		if (!t.isNull()) {
			attachStorage(t.myStorage);
		} else {
			attachStorage(0);
		}
	}
	return *this;
}
template<class T>
inline const weak_ptr<T> &weak_ptr<T>::operator = (const shared_ptr<T> &t) {
	detachStorage();
	attachStorage(t.myStorage);
	return *this;
}

template<class T>
inline T* weak_ptr<T>::operator -> () const {
	return (myStorage == 0) ? 0 : myStorage->pointer();
}
template<class T>
inline T& weak_ptr<T>::operator * () const {
	return myStorage->content();
}
template<class T>
inline bool weak_ptr<T>::isNull() const {
	return myStorage == 0 || myStorage->pointer() == 0;
}
template<class T>
inline void weak_ptr<T>::reset() {
	detachStorage();
	attachStorage(0);
}
template<class T>
inline bool weak_ptr<T>::operator == (const weak_ptr<T> &t) const {
	return operator -> () == t.operator -> ();
}
template<class T>
inline bool weak_ptr<T>::operator != (const weak_ptr<T> &t) const {
	return !operator == (t);
}
template<class T>
inline bool weak_ptr<T>::operator < (const weak_ptr<T> &t) const {
	return operator -> () < t.operator -> ();
}
template<class T>
inline bool weak_ptr<T>::operator > (const weak_ptr<T> &t) const {
	return t.operator < (*this);
}
template<class T>
inline bool weak_ptr<T>::operator <= (const weak_ptr<T> &t) const {
	return !t.operator < (*this);
}
template<class T>
inline bool weak_ptr<T>::operator >= (const weak_ptr<T> &t) const {
	return !operator < (t);
}
template<class T>
inline bool weak_ptr<T>::operator == (const shared_ptr<T> &t) const {
	return operator -> () == t.operator -> ();
}
template<class T>
inline bool weak_ptr<T>::operator != (const shared_ptr<T> &t) const {
	return !operator == (t);
}
template<class T>
inline bool weak_ptr<T>::operator < (const shared_ptr<T> &t) const {
	return operator -> () < t.operator -> ();
}
template<class T>
inline bool weak_ptr<T>::operator > (const shared_ptr<T> &t) const {
	return t.operator < (*this);
}
template<class T>
inline bool weak_ptr<T>::operator <= (const shared_ptr<T> &t) const {
	return !t.operator < (*this);
}
template<class T>
inline bool weak_ptr<T>::operator >= (const shared_ptr<T> &t) const {
	return !operator < (t);
}

#endif /* __SHARED_PTR_H__ */
