package com.formacionbdi.microservicios.app.cursos.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.formacionbdi.microservicios.commons.alumnos.models.entity.Alumno;

@FeignClient(name = "microservicios-alumnos")
public interface AlumnoFeignClient {
	@GetMapping("/alumnos-por-curso")
	public Iterable<Alumno> buscarAlumnosPorCurso(@RequestParam Iterable<Long> ids);
}
