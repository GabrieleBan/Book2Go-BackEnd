package com.b2g.rentalservice.service;

import com.b2g.commons.RentalFormatCreationDTO;
import com.b2g.commons.RentalOptionCreateDTO;
import com.b2g.rentalservice.dto.RetrieveFormatsOptionsDTO;
import com.b2g.rentalservice.model.*;

import com.b2g.rentalservice.repository.PhysicalBookRepository;
import com.b2g.rentalservice.repository.RentalBookFormatRepository;
import com.b2g.rentalservice.repository.RentalOptionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalService {



    private final RabbitTemplate rabbitTemplate;
    @Value("${app.rabbitmq.exchange}")
    private  String exchangeName;
    @Value("${app.rabbitmq.queue.name}")
    private String queueNamePrefix;
//    @Value("${app.rabbitmq.routingkey.book.creation}")
//    private String routingKeyParentBookCreated;

    @Autowired
    @Qualifier("safeRabbitTemplate")
    private final RabbitTemplate safeRabbitTemplate;
    @Autowired
    private RentalBookFormatRepository rentalBookFormatRepository;
    @Autowired
    private PhysicalBookRepository physicalBookRepository;
    @Autowired
    private RentalOptionRepository rentalOptionRepository;
    private final PhysicalBookService physicalBookService;

    public Set<PhysicalBookIdentifier> addRentalFormat(@Valid RentalFormatCreationDTO formatDTO) throws Exception {
        RentalBookFormat rentalBookFormat =  RentalBookFormat.builder()
                .formatId(formatDTO.formatId())
                .bookId(formatDTO.parentBookId())
                .isAvailableOnSubscription(formatDTO.isAvailableOnSubscription())
                .stockQuantity(formatDTO.stockQuantity())
                .formatType(FormatType.valueOf(formatDTO.formatType()))
                .build();
        log.info("RentalBookFormat added: " + rentalBookFormat);
        rentalBookFormatRepository.save(rentalBookFormat);
        log.info("RentalBookFormat saved: " + rentalBookFormat);
        if(! (formatDTO.formatType().equalsIgnoreCase(FormatType.AUDIOBOOK.toString()) ||
        formatDTO.formatType().equalsIgnoreCase(FormatType.EBOOK.toString()))) {
            if(formatDTO.stockQuantity()==null || formatDTO.stockQuantity()<=0) {
                throw new Exception("Quantity should be > 0 for physical books");
            }
            log.info("Aggiungendo nuovi libri fisici " + formatDTO.stockQuantity());
            return physicalBookService.addPhysicalBooks(rentalBookFormat.getFormatId(),formatDTO.stockQuantity() );
        }
        else
            return new HashSet<>();
    }

    public RentalOption addOrCreateOptionTo(UUID formatId, @Valid RentalOptionCreateDTO optionDTO) throws Exception {
        Optional<RentalBookFormat> format= rentalBookFormatRepository.findById(formatId);
        if (format.isPresent()) {
            RentalBookFormat rentalBookFormat = format.get();
            RentalOption option= getOrCreateNewRentalOption(optionDTO,formatId);

            rentalBookFormat.getRentalOptions().add(option);
            rentalBookFormatRepository.save(rentalBookFormat);
            return option;
        }
        else {
            throw new Exception("Format not found in Rental System");
        }
    }

    private RentalOption getOrCreateNewRentalOption(@Valid RentalOptionCreateDTO optionDTO, UUID rentalBookFormat) {
        Optional<RentalOption> option=rentalOptionRepository.findByDurationDaysAndPriceAndDescription(optionDTO.durationDays(),optionDTO.price(),optionDTO.description());
        if (option.isPresent()) {
            RentalOption rentalOption = option.get();
            log.info("RentalOption created: " +  rentalOption);
            return rentalOption;
        }
        else {

            RentalOption newOption=RentalOption.builder()
                    .bookFormat(rentalBookFormat)
                    .price(optionDTO.price())
                    .description(optionDTO.description())
                    .durationDays(optionDTO.durationDays()).build();
            newOption= rentalOptionRepository.save(newOption);
            log.info("RentalOption saved: " + newOption);
            return newOption;
        }
    }

    public RetrieveFormatsOptionsDTO getFormatRentOptions(UUID formatId) throws Exception {
        Optional<RentalBookFormat> tmp= rentalBookFormatRepository.findById(formatId);
        if(tmp.isPresent()) {
            RentalBookFormat rentalBookFormat = tmp.get();
            return RetrieveFormatsOptionsDTO.builder()
                    .rentalBookFormatId(formatId)
                    .parentBook(rentalBookFormat.getBookId())
                    .format(rentalBookFormat.getFormatType())
                    .options(rentalBookFormat.getRentalOptions())
                    .build();
        }
        else throw new Exception("Format not found in Rental System");


    }
}
