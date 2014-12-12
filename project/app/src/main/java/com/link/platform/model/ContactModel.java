package com.link.platform.model;

import com.link.platform.item.ContactItem;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by danyang.ldy on 2014/12/11.
 */
public class ContactModel extends BaseModel {

    public static ContactModel Instance = null;
    public static ContactModel getInstance() {
        if(Instance == null) {
            synchronized (ContactModel.class) {
                if(Instance == null) {
                    Instance = new ContactModel();
                    BaseModel.register(Instance);
                }
            }
        }
        return Instance;
    }

    private Set<ContactItem> contacts;

    private ContactModel() {
        contacts = new HashSet<ContactItem>();
    }

    public void addContact(ContactItem contact) {
        contacts.add(contact);
    }

    public void addContacts(List<ContactItem> list) {
        contacts.addAll(list);
    }

    public void removeContact(String IP) {
        Iterator<ContactItem> iter = contacts.iterator();
        while( iter.hasNext() ) {
            if( IP.equals( iter.next().IP ) ) {
                iter.remove();
                break;
            }
        }
    }

    public ContactItem getContact(String IP) {
        Iterator<ContactItem> iter = contacts.iterator();
        while( iter.hasNext() ) {
            ContactItem item = iter.next();
            if( IP.equals( item.IP ) ) {
                return item;
            }
        }
        return null;
    }

    @Override
    protected void exit() {
        contacts.clear();
    }
}
