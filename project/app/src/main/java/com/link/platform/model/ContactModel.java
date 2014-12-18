package com.link.platform.model;

import com.link.platform.item.ContactItem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    private Map<String, ContactItem> contacts;

    private ContactModel() {
        contacts = new HashMap<String, ContactItem>();
        register(this);
    }

    public void addContact(ContactItem contact) {
        contacts.put(contact.IP, contact);
    }

    public void addContacts(Map<String, ContactItem> list, boolean clear) {
        if( clear ) {
            contacts.clear();
        }
        contacts.putAll(list);
    }

    public void removeContact(String IP) {
        contacts.remove( IP );
    }

    public ContactItem getContact(String IP) {
        return contacts.get(IP);
    }

    @Override
    protected void exit() {
        contacts.clear();
    }
}
