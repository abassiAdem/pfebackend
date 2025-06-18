package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ChampDTO;
import com.example.demo.dto.ChampRequest;
import com.example.demo.dto.FormulaireDTO;
import com.example.demo.dto.OptionDTO;
import com.example.demo.dto.TypeDTO;
import com.example.demo.dto.TypeRequest;
import com.example.demo.entities.Champ;
import com.example.demo.entities.Formulaire;
import com.example.demo.entities.Option;
import com.example.demo.entities.Type;
import com.example.demo.repository.ChampRepository;
import com.example.demo.repository.FormulaireRepository;
import com.example.demo.repository.OptionRepository;
import com.example.demo.repository.TypeRepository;

import jakarta.transaction.Transactional;

@Service
public class FormulaireService {

    @Autowired private final TypeRepository typeRepository;
    @Autowired private final FormulaireRepository formulaireRepository;
    @Autowired private final ChampRepository champRepository;
    @Autowired private final OptionRepository optionRepository;

    public FormulaireService(TypeRepository typeRepository, FormulaireRepository formulaireRepository,
                              ChampRepository champRepository, OptionRepository optionRepository) {
        this.typeRepository = typeRepository;
        this.formulaireRepository = formulaireRepository;
        this.champRepository = champRepository;
        this.optionRepository = optionRepository;
    }
    public boolean existsByName(String name) {
        return typeRepository.findByName(name).isPresent();
    }
    @Transactional
    public Type createTypeWithForm(TypeRequest request) {
        if (typeRepository.findByName(request.getTypeName()).isPresent()) {
            throw new RuntimeException("Type already exists with name: " + request.getTypeName());
        }

        Type type = new Type();
        type.setName(request.getTypeName());
        type.setStatus(request.getStatus());
        type.setDureeEstimee(request.getDureeEstimee());
        Formulaire formulaire = new Formulaire();
        formulaire.setValide(request.getFormulaireValide());
        formulaire.setTypeformulaire(type);
        type.setFormulaire(formulaire);

        Type savedType = typeRepository.save(type);
        formulaireRepository.save(formulaire);

        for (ChampRequest champReq : request.getChamps()) {
            Champ champ = new Champ();
            champ.setNom(champReq.getNom());
            champ.setType(champReq.getType());
            champ.setObligatoire(champReq.getObligatoire());
            champ.setFormulaire(formulaire);

            Champ savedChamp = champRepository.save(champ);

            if (champReq.getOptions() != null) {
                for (String opt : champReq.getOptions()) {
                    Option option = new Option();
                    option.setValeur(opt);
                    option.setChamp(savedChamp);
                    optionRepository.save(option);
                }
            }
        }

        return savedType;
    }

    public List<TypeDTO> findAllTypesWithFormulairesAndChamps() {
        Set<Type> typesSet = typeRepository.findAllWithFormulairesAndChamps();
        return typesSet.stream().map(this::convertToTypeDTO).collect(Collectors.toList());
    }

    public List<TypeDTO> findAllTypesWithFormulairesAndChampsNotNormal() {
        Set<Type> typesSet = typeRepository.findAllWithFormulairesAndChampsNotNormal();
        return typesSet.stream().map(this::convertToTypeDTO).collect(Collectors.toList());
    }

    public Optional<Type> findTypeById(Long id) {
        return typeRepository.findById(id);
    }

    public Optional<Type> updateStatus(Long id, String status) {
        return typeRepository.findById(id).map(type -> {
            type.setStatus(status);
            return typeRepository.save(type);
        });
    }

    @Transactional
    public boolean deleteType(Long id) {
        return typeRepository.findById(id).map(type -> {
            typeRepository.delete(type);
            return true;
        }).orElse(false);
    }

    @Transactional
    public Optional<Type> updateType(Long typeId, TypeRequest typeRequest) {
        Optional<Type> typeOpt = typeRepository.findById(typeId);
        if (typeOpt.isEmpty()) return Optional.empty();

        Type type = typeOpt.get();
        type.setName(typeRequest.getTypeName());
        type.setStatus(typeRequest.getStatus());
        type.setDureeEstimee(typeRequest.getDureeEstimee());

        Formulaire formulaire = type.getFormulaire();
        if (formulaire == null) {
            formulaire = new Formulaire();
            formulaire.setTypeformulaire(type);
        }
        formulaire.setValide(typeRequest.getValide());

        Set<Champ> existingChamps = new HashSet<>(formulaire.getChamps());
        List<Champ> updatedChamps = new ArrayList<>();

        for (ChampRequest champReq : typeRequest.getChamps()) {
            Champ champ = existingChamps.stream()
                .filter(c -> Objects.equals(c.getId(), champReq.getId()))
                .findFirst().orElse(new Champ());

            champ.setNom(champReq.getNom());
            champ.setType(champReq.getType());
            champ.setObligatoire(champReq.getObligatoire());
            champ.setFormulaire(formulaire);

            if (champReq.getOptions() != null) {
                champ.getOptions().removeIf(opt -> !champReq.getOptions().contains(opt.getValeur()));

                List<Option> newOptions = champReq.getOptions().stream()
                    .filter(optVal -> champ.getOptions().stream().noneMatch(o -> o.getValeur().equals(optVal)))
                    .map(val -> {
                        Option opt = new Option();
                        opt.setValeur(val);
                        opt.setChamp(champ);
                        return opt;
                    }).collect(Collectors.toList());

                champ.getOptions().addAll(newOptions);
            } else {
                champ.getOptions().clear();
            }

            updatedChamps.add(champ);
        }

        formulaire.getChamps().clear();
        formulaire.getChamps().addAll(updatedChamps);

        formulaireRepository.save(formulaire);
        return Optional.of(typeRepository.save(type));
    }

    @Transactional
    public void deleteChamp(Long champId) {
        champRepository.findById(champId).ifPresent(champ -> {
            Formulaire parent = champ.getFormulaire();
            if (parent != null) {
                parent.removeChamp(champ);
                formulaireRepository.save(parent);
            }
        });
    }

    public Optional<Champ> addFieldToType(Long typeId, ChampRequest champRequest) {
        Optional<Type> typeOpt = typeRepository.findById(typeId);
        if (typeOpt.isEmpty()) return Optional.empty();

        Type type = typeOpt.get();
        Formulaire formulaire = type.getFormulaire();
        if (formulaire == null) return Optional.empty();

        Champ champ = new Champ();
        champ.setNom(champRequest.getNom());
        champ.setType(champRequest.getType());
        champ.setObligatoire(champRequest.getObligatoire());
        champ.setFormulaire(formulaire);

        if (champRequest.getOptions() != null) {
            List<Option> options = champRequest.getOptions().stream().map(val -> {
                Option opt = new Option();
                opt.setValeur(val);
                opt.setChamp(champ);
                return opt;
            }).collect(Collectors.toList());
            champ.setOptions(options);
        }

        champRepository.save(champ);
        formulaire.getChamps().add(champ);
        formulaireRepository.save(formulaire);
        typeRepository.save(type);

        return Optional.of(champ);
    }

    public List<FormulaireDTO> getFormulaireByTypeId(Long typeId) {
        Type type = typeRepository.findById(typeId)
                .orElseThrow(() -> new RuntimeException("Type not found with id: " + typeId));

        List<Formulaire> formulaires = formulaireRepository.findByTypeformulaire(type);
        if (formulaires.isEmpty()) throw new RuntimeException("Aucune formulaire trouvée pour le type id : " + typeId);

        return formulaires.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<FormulaireDTO> getFormulaireByTypeName(String typename) {
        Type type = typeRepository.findByName(typename)
                .orElseThrow(() -> new RuntimeException("Type not found with name: " + typename));

        List<Formulaire> formulaires = formulaireRepository.findByTypeformulaire(type);
        if (formulaires.isEmpty()) throw new RuntimeException("Aucune formulaire trouvée pour le type name : " + typename);

        return formulaires.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private FormulaireDTO mapToDTO(Formulaire formulaire) {
        FormulaireDTO dto = new FormulaireDTO();
        dto.setId(formulaire.getId());
        dto.setValide(formulaire.getValide());

        List<ChampDTO> champsDto = formulaire.getChamps().stream().map(champ -> {
            ChampDTO champDTO = new ChampDTO();
            champDTO.setId(champ.getId());
            champDTO.setNom(champ.getNom());
            champDTO.setType(champ.getType());
            champDTO.setObligatoire(champ.getObligatoire());

            List<OptionDTO> optionDTOs = champ.getOptions().stream().map(opt -> {
                OptionDTO optDTO = new OptionDTO();
                optDTO.setId(opt.getId());
                optDTO.setValeur(opt.getValeur());
                return optDTO;
            }).collect(Collectors.toList());

            champDTO.setOptions(optionDTOs);
            return champDTO;
        }).collect(Collectors.toList());

        dto.setChamps(champsDto);
        return dto;
    }

    private TypeDTO convertToTypeDTO(Type type) {
        TypeDTO dto = new TypeDTO();
        dto.setId(type.getId());
        dto.setDureeEstimee(type.getDureeEstimee());
        dto.setName(type.getName());
        dto.setStatus(type.getStatus());
        dto.setDateCreation(type.getDateCreation());
        dto.setDateModification(type.getDateModification());

        if (type.getFormulaire() != null) {
            FormulaireDTO formulaireDTO = new FormulaireDTO();
            formulaireDTO.setId(type.getFormulaire().getId());
            formulaireDTO.setValide(type.getFormulaire().getValide());

            List<ChampDTO> champDTOs = type.getFormulaire().getChamps().stream()
            	    .map(champ -> {
            	        ChampDTO champDTO = new ChampDTO();
            	        champDTO.setId(champ.getId());
            	        champDTO.setNom(champ.getNom());
            	        champDTO.setType(champ.getType());
            	        champDTO.setObligatoire(champ.getObligatoire());
            	        
            	        List<OptionDTO> optionDTOs = champ.getOptions().stream()
            	            .map(option -> {
            	                OptionDTO optionDTO = new OptionDTO();
            	                optionDTO.setId(option.getId());
            	                optionDTO.setValeur(option.getValeur());
            	                return optionDTO;
            	            })
            	            .collect(Collectors.toList());
            	        champDTO.setOptions(optionDTOs);
            	        
            	        return champDTO;
            	    })
            	    .collect(Collectors.toList());


            formulaireDTO.setChamps(champDTOs);
            dto.setFormulaire(formulaireDTO);
        }

        return dto;
    }

}

