package com.formacionbdi.microservicios.app.cursos.controllers;

import com.formacionbdi.microservicios.commons.alumnos.models.entity.Alumno;
import com.formacionbdi.microservicios.commons.controllers.CommonController;
import com.formacionbdi.microservicios.commons.examenes.models.entity.Examen;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.formacionbdi.microservicios.app.cursos.models.entity.Curso;
import com.formacionbdi.microservicios.app.cursos.models.entity.CursoAlumno;
import com.formacionbdi.microservicios.app.cursos.services.CursoService;

@RestController
public class CursoController extends CommonController<Curso, CursoService> {

	/* MÉTODOS GET */

	/*
	 * Devuelve todos los cursos. Cada curso tiene una lista del tipo Alumno donde
	 * cada alumno sólo contienen el campo id.
	 */
	@GetMapping
	@Override
	public ResponseEntity<?> listar() {
		List<Curso> cursos = ((List<Curso>) commonService.findAll()).stream().map(c -> {
			c.getCursoAlumnos().forEach(ca -> {
				Alumno alumno = new Alumno();
				alumno.setId(ca.getAlumnoId());
				c.addAlumno(alumno);
			});
			return c;
		}).collect(Collectors.toList());
		return ResponseEntity.ok().body(cursos);
	}

	/*
	 * Devuelve todos los cursos con paginación. Cada curso tiene una lista del tipo
	 * Alumno donde cada alumno sólo contienen el campo id.
	 */
	@GetMapping("/pagina")
	public ResponseEntity<?> listar(Pageable pageable) {
		// Capturamos el objeto del tipo Page para poder manipular los datos (nº página,
		// cursos por página, etc).
		Page<Curso> cursos = commonService.findAll(pageable).map(c -> {
			c.getCursoAlumnos().forEach(ca -> {
				Alumno alumno = new Alumno();
				alumno.setId(ca.getAlumnoId());
				c.addAlumno(alumno);
			});
			return c;
		});
		return ResponseEntity.ok().body(cursos);
	}

	// Devuelve todos los Alumnos (con todos sus campos) del curso que se pide. La
	// solicitud se hace a otro microservicio.
	@GetMapping("/{id}")
	@Override
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		Optional<Curso> o = commonService.findById(id);
		if (o.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Curso curso = o.get();
		if (curso.getCursoAlumnos().isEmpty() == false) {
			List<Long> ids = curso.getCursoAlumnos().stream().map(ca -> ca.getAlumnoId()).collect(Collectors.toList());
			List<Alumno> alumnos = (List<Alumno>) this.commonService.buscarAlumnosPorCurso(ids);
			curso.setAlumnos(alumnos);
		}
		return ResponseEntity.ok().body(curso);
	}

	/*
	 * Pedimos el curso por el id del alumno. Antes de que se nos devuelva dicho
	 * curso, éste actualiza el curso con los exámenes realizados por ese alumno (en
	 * el caso de que los haya).
	 */
	@GetMapping("/alumno/{id}")
	public ResponseEntity<?> buscarCursoPorAlumnoId(@PathVariable Long id) {
		Curso curso = this.commonService.findCursoByAlumnoId(id);
		if (curso != null) {
			// Pedimos (request) mediante API REST los ids de los exámenes respondidos por
			// este alumno.
			List<Long> examenesIds = (List<Long>) this.commonService.obtenerExamenesIdsConRespuestasAlumno(id);
			/*
			 * Creamos una nueva lista del tipo Examen. Esta lista contendrá todos los
			 * objetos Examen cuyos id's coincidan con la lista de examenes de este curso,
			 * pero no se manipula nada del objeto curso.
			 */
			List<Examen> examenes = curso.getExamenes().stream().map(examen -> {
				/*
				 * Comprobamos si cada examen del curso ha sido respondido por el alumno. Si es
				 * así lo establecemos como true en cada objeto Examen de la lista. Recordemos
				 * que, llegados a este punto, no hemos manipulado el objeto curso en ningún
				 * momento.
				 */
				if (examenesIds.contains(examen.getId())) {
					examen.setRespondido(true);
				}
				// Devolvemos el examen para que sea devuelto en el stream final.
				return examen;
			}).collect(Collectors.toList());
			// Ahora si guardamos (persistimos) los exámenes realizados por el alumno en el
			// objeto curso.
			curso.setExamenes(examenes);
		}
		return ResponseEntity.ok(curso);
	}

	/* MÉTODOS PUT */

	@PutMapping("/{id}")
	public ResponseEntity<?> modificarCurso(@Valid @RequestBody Curso curso, BindingResult result,
			@PathVariable Long id) {
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

	// Agrega alumnos al curso.
	@PutMapping("/{id}/asignar-alumnos")
	public ResponseEntity<?> asignarAlumnosACurso(@RequestBody List<Alumno> alumnos, @PathVariable Long id) {
		Optional<Curso> o = this.commonService.findById(id);
		if (o.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		Curso cursoDb = o.get();
		alumnos.forEach(a -> {
			CursoAlumno cursoAlumno = new CursoAlumno();
			cursoAlumno.setAlumnoId(a.getId());
			cursoAlumno.setCurso(cursoDb);
			cursoDb.addCursoAlumno(cursoAlumno);
		});
		
		return ResponseEntity.status(HttpStatus.CREATED).body(this.commonService.save(cursoDb));
	}

	// Elimina el alumno del curso.
	@PutMapping("/{id}/eliminar-alumno")
	public ResponseEntity<?> eliminarAlumnoDeCurso(@RequestBody Alumno alumno, @PathVariable Long id) {
		Optional<Curso> o = this.commonService.findById(id);
		if (o.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		Curso cursoDb = o.get();
		CursoAlumno cursoAlumno = new CursoAlumno();
		cursoAlumno.setAlumnoId(alumno.getId());
		/*
		 * Enviamos a eliminar el objeto cursoAlumno al objeto cursoDb a través del
		 * método removeCursoAlumno. La clase Curso, gracias a 'cascade =
		 * CascadeType.ALL', notifica a la BBDD que debe eliminarse el valor del campo
		 * curso que se encuentra en la clase CursoAlumno.
		 */
		cursoDb.removeCursoAlumno(cursoAlumno);

		return ResponseEntity.status(HttpStatus.CREATED).body(this.commonService.save(cursoDb));
	}

	// Agrega examenes al curso.
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

	// Elimina examenes del curso.
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

	/* MÉTODOS DELETE */

	@DeleteMapping("/eliminar-alumno/{id}")
	public ResponseEntity<?> eliminarCursoAlumnoPorId(@PathVariable Long id) {
		this.commonService.eliminarCursoAlumnoPorId(id);
		return ResponseEntity.noContent().build();
	}

}