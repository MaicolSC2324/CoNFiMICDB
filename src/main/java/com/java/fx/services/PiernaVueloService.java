package com.java.fx.services;

import com.java.fx.models.PiernaVuelo;
import com.java.fx.models.PiernaVueloId;
import com.java.fx.repositories.PiernaVueloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PiernaVueloService {

    @Autowired
    private PiernaVueloRepository piernaVueloRepository;

    public PiernaVuelo save(PiernaVuelo piernaVuelo) {
        if (piernaVuelo.getIdPierna() == null || piernaVuelo.getIdPierna().isEmpty()) {
            piernaVuelo.setIdPierna(piernaVuelo.getNoHojaLibro() + "-" + piernaVuelo.getNoPierna());
        }
        piernaVuelo.setTiempoVuelo(piernaVuelo.calcularTiempoVuelo());
        return piernaVueloRepository.save(piernaVuelo);
    }

    public Optional<PiernaVuelo> findById(Integer noHojaLibro, Integer noPierna) {
        return piernaVueloRepository.findById(new PiernaVueloId(noHojaLibro, noPierna));
    }

    public Optional<PiernaVuelo> findByIdPierna(String idPierna) {
        return piernaVueloRepository.findByIdPierna(idPierna);
    }

    public List<PiernaVuelo> findByNoHojaLibro(Integer noHojaLibro) {
        return piernaVueloRepository.findByNoHojaLibroOrderByNoPiernaAsc(noHojaLibro);
    }

    public List<String> findDistinctOrigenes() {
        return piernaVueloRepository.findDistinctOrigenes();
    }

    public List<String> findDistinctDestinos() {
        return piernaVueloRepository.findDistinctDestinos();
    }

    public void delete(PiernaVuelo piernaVuelo) {
        piernaVueloRepository.delete(piernaVuelo);
    }

    public Integer getNextNoPierna(Integer noHojaLibro) {
        Integer count = piernaVueloRepository.countByNoHojaLibro(noHojaLibro);
        return count + 1;
    }
}

