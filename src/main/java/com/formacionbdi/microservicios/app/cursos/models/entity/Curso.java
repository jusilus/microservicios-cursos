package com.formacionbdi.microservicios.app.cursos.models.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.formacionbdi.microservicios.commons.alumnos.models.entity.Alumno;
import com.formacionbdi.microservicios.commons.examenes.models.entity.Examen;

@Entity
@Table(name="cursos")
public class Curso {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotEmpty
	private String nombre;
	
	@Column(name="create_at")
	private Date createAt;
	
	//Evitamos un bucle infinito cuando pedimos información a la tabla CursoAlumno (campo curso)
	@JsonIgnoreProperties(value= {"curso"}, allowSetters = true)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)	
	private List<CursoAlumno> cursoAlumnos;
	
	//@OneToMany(fetch = FetchType.LAZY) /* De cuando la tabla estaba en MySQL */
	@Transient //Es un atributo que se rellena después (una vez se llame al otro microservicio)
	private List<Alumno> alumnos;
	
	@ManyToMany(fetch = FetchType.LAZY)
	private List<Examen> examenes;
	
	@PrePersist	
	@Temporal(TemporalType.TIMESTAMP)
	public void prePersist() {
		this.createAt = new Date();
	}
	
	public Curso() {
		this.alumnos = new ArrayList<>();
		this.examenes = new ArrayList<>();
		this.cursoAlumnos = new ArrayList<>();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Date getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Date createAt) {
		this.createAt = createAt;
	}

	public List<Alumno> getAlumnos() {
		return alumnos;
	}

	public void setAlumnos(List<Alumno> alumnos) {
		this.alumnos = alumnos;
	}
	
	public void addAlumno(Alumno alumno) {
		this.alumnos.add(alumno);
	}
	
	public void removeAlumno(Alumno alumno) {
		this.alumnos.remove(alumno);
	}

	public List<Examen> getExamenes() {
		return examenes;
	}

	public void setExamenes(List<Examen> examenes) {
		this.examenes = examenes;
	}
	
	public List<CursoAlumno> getCursoAlumnos() {
		return cursoAlumnos;
	}

	public void setCursoAlumnos(List<CursoAlumno> cursoAlumnos) {
		this.cursoAlumnos = cursoAlumnos;
	}

	public void addExamen(Examen examen) {
		this.examenes.add(examen);
	}
	
	public void removeExamen(Examen examen) {
		this.examenes.remove(examen);
	}
	
	public void addCursoAlumno(CursoAlumno cursoAlumno) {
		this.cursoAlumnos.add(cursoAlumno);
	}
	
	public void removeCursoAlumno(CursoAlumno cursoAlumno) {
		this.cursoAlumnos.remove(cursoAlumno);
	}	
}