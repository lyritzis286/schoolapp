package gr.aueb.cf.schoolapp.service;

import gr.aueb.cf.schoolapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.schoolapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.schoolapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.schoolapp.dto.TeacherEditDTO;
import gr.aueb.cf.schoolapp.dto.TeacherInsertDTO;
import gr.aueb.cf.schoolapp.dto.TeacherReadOnlyDTO;
import gr.aueb.cf.schoolapp.mapper.Mapper;
import gr.aueb.cf.schoolapp.model.Region;
import gr.aueb.cf.schoolapp.model.Teacher;
import gr.aueb.cf.schoolapp.repository.RegionRepository;
import gr.aueb.cf.schoolapp.repository.TeacherRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeacherService implements ITeacherService {

    private final TeacherRepository teacherRepository;
    private final RegionRepository regionRepository;
    private final Mapper mapper;

    @Override
    @Transactional(rollbackFor = {EntityInvalidArgumentException.class, EntityAlreadyExistsException.class})
    public TeacherReadOnlyDTO saveTeacher(TeacherInsertDTO dto)
            throws EntityInvalidArgumentException, EntityAlreadyExistsException {

        try{
//            if (dto.vat() != null && teacherRepository.findByVat(dto.vat()).isPresent()) {
            if (dto.vat() != null && isTeacherExistsByVat(dto.vat())) {
                throw new EntityAlreadyExistsException("Teacher with VAT " + dto.vat() + " already exists");
            }
            Region region = regionRepository.findById(dto.regionId())
                    .orElseThrow(() ->
                            new EntityInvalidArgumentException
                                    ("Region with ID "
                                            + dto.regionId() + " does not exist"));

            Teacher teacher = mapper.mapToTeacherEntity(dto);
            region.addTeacher(teacher);
            teacherRepository.save(teacher);        //pre-persist
            log.info("Teacher with VAT={} save successfully ", dto.vat()); //Structured Logging
            return mapper.mapToTeacherReadOnlyDTO(teacher);


        } catch (EntityAlreadyExistsException e){
            log.warn("Save failed for teacher with VAT= {}.Teacher already exists", dto.vat());
            throw e;
        } catch (EntityInvalidArgumentException e){
            log.warn("Save failed for teacher with VAT= {}. Region with ID= {} invalid", dto.vat(), dto.regionId());
            throw e;
        } catch (DataIntegrityViolationException e){
            log.warn("Save failed for teacher with VAT= {}. Teacher exists", dto.vat());
            throw new EntityAlreadyExistsException("Save failed for teacher with VAT=  " + dto.vat() + " already exists");
        }
    }

    @Override
    @Transactional(rollbackFor = {EntityInvalidArgumentException.class, EntityNotFoundException.class, EntityAlreadyExistsException.class})
    public TeacherReadOnlyDTO updateTeacher(TeacherEditDTO dto) throws EntityInvalidArgumentException, EntityNotFoundException, EntityAlreadyExistsException {
        try {
            Teacher teacher = teacherRepository.findByUuidAndDeletedFalse(dto.uuid())
                    .orElseThrow(() -> new EntityNotFoundException("Teacher with UUID " + dto.uuid() + " does not exist"));

            if(!teacher.getVat().equals(dto.vat())) {  //TODO - Use object utility class for null safety
                if(teacherRepository.findByVatAndDeletedFalse(dto.vat()).isPresent()) {
                    throw new EntityAlreadyExistsException("Teacher with VAT " + dto.vat() + " already exists");
                }
                teacher.setVat(dto.vat());
            }
            teacher.setFirstname(dto.firstname());
            teacher.setLastname(dto.lastname());

            if(!Objects.equals(teacher.getRegion().getId(), dto.regionId())) {
                Region region = regionRepository.findById(dto.regionId())
                        .orElseThrow(() -> new EntityInvalidArgumentException("Region ID= " + dto.regionId() + " not found"));
                Region oldRegion = teacher.getRegion();

                if(oldRegion != null) {
                    oldRegion.removeTeacher(teacher);
                }
                region.addTeacher(teacher);
            }

            teacherRepository.save(teacher);
            log.info("Teacher with VAT={} update successfully ", dto.vat());
            return mapper.mapToTeacherReadOnlyDTO(teacher);

        }catch (EntityNotFoundException e){
            log.warn("Update failed for teacher with uuid={}. Teacher not found", dto.uuid());
            throw e;

        }catch (EntityAlreadyExistsException e) {
            log.warn("Update failed for teacher with uuid={}. Teacher already exists", dto.uuid());
            throw e;

        }catch (EntityInvalidArgumentException e) {
            log.warn("Update failed for teacher with uuid={}. Region not found", dto.uuid());
            throw e;

        }
   }

    @Override
    @Transactional(readOnly = true)
    public Page<TeacherReadOnlyDTO> getPaginatedTeachersDeletedFalse(Pageable pageable) {
        Page<Teacher> teacherPage = teacherRepository.findAllByAndDeletedFalse(pageable);
        log.debug("Get paginated teachers not deleted returned successfully page= {}, size= {}", teacherPage.getNumber(), teacherPage.getSize());
        return teacherPage.map(mapper::mapToTeacherReadOnlyDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeacherReadOnlyDTO> getPaginatedTeachers(Pageable pageable) {
        Page<Teacher> teacherPage = teacherRepository.findAll(pageable);
        log.debug("Get paginated teachers returned successfully page= {}, size= {}", teacherPage.getNumber(), teacherPage.getSize());
        return teacherPage.map(mapper::mapToTeacherReadOnlyDTO);
    }



    @Override
    @Transactional(readOnly = true)
    public boolean isTeacherExistsByVat(String vat) {
        return teacherRepository.findByVat(vat).isPresent();
    }
}
