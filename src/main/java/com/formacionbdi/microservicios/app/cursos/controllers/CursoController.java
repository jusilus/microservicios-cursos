package com.formacionbdi.microservicios.app.cursos.controllers;

import com.formacionbdi.microservicios.commons.alumnos.models.entity.Alumno;
import com.formacionbdi.microservicios.commons.controllers.CommonController;
import com.formacionbdi.microservicios.commons.examenes.models.entity.Examen;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
	public ResponseEntity<?> modificarCurso(@Valid @RequestBody Curso curso, BindingResult result, @PathVariable Long id) {
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
	public ResponseEntity<?> asignarAlumnosACurso(@RequestBody List<Alumno> alumnos, @PathVariable Long id) {
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
	public ResponseEntity<?> eliminarAlumnoDeCurso(@RequestBody Alumno alumno, @PathVariable Long id) {
		Optional<Curso> o = this.commonService.findById(id);
		if (o.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		Curso cursoDb = o.get();
		cursoDb.removeAlumno(alumno);
		return ResponseEntity.status(HttpStatus.CREATED).body(this.commonService.save(cursoDb));
	}
	/*
	 * Pedimos el curso por el id del alumno. Antes de que se nos devuelva dicho curso, éste se actualiza el curso con los 
	 * exámenes realizados por ese alumno (en el caso de que los haya).
	 */
	@GetMapping("/alumno/{id}")
	public ResponseEntity<?> buscarCursoPorAlumnoId(@PathVariable Long id) {
		Curso curso = this.commonService.findCursoByAlumnoId(id);
		if (curso != null) {
			// Pedimos (request) mediante API REST los ids de los exámenes respondidos por este alumno.
			List<Long> examenesIds = (List<Long>)this.commonService.obtenerExamenesIdsConRespuestasAlumno(id);
			/*
			 * Creamos una nueva lista del tipo Examen. Esta lista contendrá todos los objetos Examen cuyos
			 * id's coincidan con la lista de examenes de este curso, pero no se manipula nada del objeto curso.
			 */
			List<Examen> examenes = curso.getExamenes().stream().map(examen -> {
				/*
				 * Comprobamos si cada examen del curso ha sido respondido por el alumno. Si es
				 * así lo establecemos como true en cada objeto Examen de la lista. Recordemos que, llegados a
				 * este punto, no hemos manipulado el objeto curso en ningún momento.
				 */
				if (examenesIds.contains(examen.getId())) {
					examen.setRespondido(true);
				}
				//Devolvemos el examen para que sea devuelto en el stream final.
				return examen;
			}).collect(Collectors.toList());
			// Ahora si guardamos (persistimos) los exámenes realizados por el alumno en el objeto curso.			
			curso.setExamenes(examenes);
		}
		return ResponseEntity.ok(curso);
	}

	@PutMapping("/{id}/asignar-examenes")
	public ResponseEntity<?> asignarExamenAlCurso(@RequestBody List<Examen> examenes, @PathVariable Long id) {
		Optional<Curso> o = this.commonService.findById(id);
		if (!o.isPresent()) {
			return ResponseEntity.noContent().build();
		}
		Curso cursoDb = o.get();
		examenes.forEach(cursoDb::addExamen);
		return ResponseEntity.status(HttpStatus.CREATED).body(this.commonService.save(cursoDb));
	}

	@PutMapping("/{id}/eliminar-examen")
	public ResponseEntity<?> eliminarExamenDelCurso(@RequestBody Examen examen, @PathVariable Long id) {
		Optional<Curso> o = this.commonService.findById(id);
		if (!o.isPresent()) {
			return ResponseEntity.noContent().build();
		}
		Curso cursoDb = o.get();
		cursoDb.removeExamen(examen);
		return ResponseEntity.status(HttpStatus.CREATED).body(this.commonService.save(cursoDb));
	}
}