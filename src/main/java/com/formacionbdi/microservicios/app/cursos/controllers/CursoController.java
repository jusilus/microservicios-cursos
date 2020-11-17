package com.formacionbdi.microservicios.app.cursos.controllers;

import com.formacionbdi.microservicios.commons.alumnos.models.entity.Alumno;
import com.formacionbdi.microservicios.commons.controllers.CommonController;
import com.formacionbdi.microservicios.commons.examenes.models.entity.Examen;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.formacionbdi.microservicios.app.cursos.models.entity.Curso;
import com.formacionbdi.microservicios.app.cursos.services.CursoService;

@RestController
public class CursoController extends CommonController<Curso, CursoService> {
	@PutMapping("/{id}")
	public ResponseEntity<?> modificar(@Valid @RequestBody Curso curso, BindingResult result, @PathVariable Long id) {
		if (result.hasErrors()) {
			return validar(result);
		}

		Optional<Curso> o = this.commonService.findById(id);
		if (o.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		Curso cursoDb = o.get();
		cursoDb.setNombre(curso.getNombre());
		return ResponseEntity.status(HttpStatus.CREATED).body(this.commonService.save(cursoDb));
	}

	@PutMapping("/{id}/asignar-alumnos")
	public ResponseEntity<?> asignarAlumnos(@RequestBody List<Alumno> alumnos, @PathVariable Long id) {
		Optional<Curso> o = this.commonService.findById(id);
		if (o.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		Curso cursoDb = o.get();
		for (Alumno a : alumnos) {
			cursoDb.addAlumno(a);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(this.commonService.save(cursoDb));
	}

	@PutMapping("/{id}/eliminar-alumno")
	public ResponseEntity<?> eliminarAlumno(@RequestBody Alumno alumno, @PathVariable Long id) {
		Optional<Curso> o = this.commonService.findById(id);
		if (o.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		Curso cursoDb = o.get();
		cursoDb.removeAlumno(alumno);
		return ResponseEntity.status(HttpStatus.CREATED).body(this.commonService.save(cursoDb));
	}

	@GetMapping("/alumno/{id}")
	public ResponseEntity<?> buscarCursoPorAlumnoId(@PathVariable Long id) {
		Curso curso = this.commonService.findCursoByAlumnoId(id);
		return ResponseEntity.ok(curso);
	}

	@PutMapping("/{id}/asignar-examenes")
	public ResponseEntity<?> asignarExamen(@RequestBody List<Examen> examenes, @PathVariable Long id) {
		Optional<Curso> o = this.commonService.findById(id);
		if (!o.isPresent()) {
			return ResponseEntity.noContent().build();
		}
		Curso cursoDb = o.get();
		examenes.forEach(cursoDb::addExamen);
		return ResponseEntity.status(HttpStatus.CREATED).body(this.commonService.save(cursoDb));
	}

	@PutMapping("/{id}/eliminar-examen")
	public ResponseEntity<?> eliminarExamen(@RequestBody Examen examen, @PathVariable Long id) {
		Optional<Curso> o = this.commonService.findById(id);
		if (!o.isPresent()) {
			return ResponseEntity.noContent().build();
		}
		Curso cursoDb = o.get();
		cursoDb.removeExamen(examen);
		return ResponseEntity.status(HttpStatus.CREATED).body(this.commonService.save(cursoDb));
	}
}