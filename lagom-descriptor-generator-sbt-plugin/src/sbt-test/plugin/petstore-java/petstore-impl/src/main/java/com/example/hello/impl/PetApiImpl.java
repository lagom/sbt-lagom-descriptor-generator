/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.example.hello.impl;

import akka.Done;
import akka.NotUsed;
import com.example.Pet;
import com.example.PetApi;
import com.example.StatusEnum;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.pcollections.PSequence;

import javax.inject.Inject;

/**
 * Implementation of the HelloService.
 */
public class PetApiImpl implements PetApi {

  @Inject
  public PetApiImpl() {
  }


  @Override
  public ServiceCall<Pet, Done> addPet() {
    return null;
  }

  @Override
  public ServiceCall<NotUsed, Done> deletePet(long petId) {
    return null;
  }

  @Override
  public ServiceCall<NotUsed, PSequence<Pet>> findPetsByStatus(PSequence<StatusEnum> status) {
    return null;
  }

  @Override
  public ServiceCall<NotUsed, PSequence<Pet>> findPetsByTags(PSequence<String> tags) {
    return null;
  }

  @Override
  public ServiceCall<NotUsed, Pet> getPetById(long petId) {
    return null;
  }

  @Override
  public ServiceCall<Pet, Done> updatePet() {
    return null;
  }

  @Override
  public ServiceCall<NotUsed, Done> updatePetWithForm(long petId) {
    return null;
  }
}
