package com.b2g.readerservice.dto;

import com.b2g.readerservice.model.Address;
import com.b2g.readerservice.model.Reader;
import org.springframework.data.jpa.domain.Specification;
public class ReaderSpecifications {

    public static Specification<Reader> usernameEquals(String username) {
        return (root, query, cb) ->
                username == null
                        ? cb.conjunction()
                        : cb.equal(root.get("username"), username);
    }

    public static Specification<Reader> nameLike(String name) {
        return (root, query, cb) ->
                name == null
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%");
    }

    public static Specification<Reader> surnameLike(String surname) {
        return (root, query, cb) ->
                surname == null
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("surname")),
                        "%" + surname.toLowerCase() + "%");
    }

    public static Specification<Reader> emailEquals(String email) {
        return (root, query, cb) ->
                email == null
                        ? cb.conjunction()
                        : cb.equal(root.get("email"), email);
    }

    public static Specification<Reader> phoneEquals(String phone) {
        return (root, query, cb) ->
                phone == null
                        ? cb.conjunction()
                        : cb.equal(root.get("phone"), phone);
    }
}