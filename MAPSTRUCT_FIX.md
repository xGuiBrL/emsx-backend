# Solución al Error de MapStruct - DepartmentMapperImpl

## Problema Identificado

El error ocurre porque Spring está intentando cargar `DepartmentMapperImpl` que fue generado previamente por MapStruct, pero la interfaz `DepartmentMapper` ya no existe (fue eliminada junto con las entidades antiguas).

Los archivos compilados antiguos en `target/` están causando el conflicto.

## Solución

### Paso 1: Limpiar el proyecto

Ejecuta el siguiente comando en la raíz del proyecto (`emsx/`):

```bash
mvn clean
```

O si usas IntelliJ IDEA:
1. Click derecho en el proyecto
2. Maven → Reload Project
3. Maven → Clean

### Paso 2: Verificar configuración de MapStruct

La configuración en `pom.xml` ya está correcta:

✅ **Dependencia MapStruct:**
```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.6.0.Beta1</version>
</dependency>
```

✅ **Annotation Processor:**
```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>${mapstruct.version}</version>
    </path>
</annotationProcessorPaths>
```

✅ **Component Model Spring:**
```xml
<compilerArgs>
    <arg>-Amapstruct.defaultComponentModel=spring</arg>
</compilerArgs>
```

### Paso 3: Recompilar el proyecto

Ejecuta:

```bash
mvn clean compile
```

O en IntelliJ IDEA:
1. Build → Rebuild Project
2. O ejecuta: `mvn clean compile` desde la terminal integrada

### Paso 4: Verificar generación

Después de compilar, verifica que los archivos se generen en:
```
target/generated-sources/annotations/com/app/emsx/mappers/
```

**Nota:** Actualmente solo debería existir `AuthMapperImpl.java` (si `AuthMapper` usa MapStruct) o ningún archivo si no usas MapStruct para los mappers restantes.

## Estado Actual

- ✅ `DepartmentMapper` - ELIMINADO (ya no existe)
- ✅ `DependentMapper` - ELIMINADO
- ✅ `EmployeeMapper` - ELIMINADO
- ✅ `SkillMapper` - ELIMINADO
- ✅ `EmployeeSkillMapper` - ELIMINADO
- ✅ `AuthMapper` - EXISTE (pero no usa MapStruct, es una clase estática)

## Verificación Final

Después de `mvn clean compile`, el proyecto debería:
1. ✅ Compilar sin errores
2. ✅ No intentar cargar `DepartmentMapperImpl`
3. ✅ Generar solo los mappers que realmente existen en el código fuente

## Si el problema persiste

1. Elimina manualmente la carpeta `target/`:
   ```bash
   rm -rf target/
   # O en Windows:
   rmdir /s /q target
   ```

2. En IntelliJ IDEA:
   - File → Invalidate Caches / Restart
   - Marca "Invalidate and Restart"

3. Vuelve a compilar:
   ```bash
   mvn clean compile
   ```

## Configuración Correcta de MapStruct

Para que MapStruct funcione correctamente, asegúrate de:

1. **Interfaz con @Mapper:**
```java
@Mapper(componentModel = "spring")
public interface MiMapper {
    // métodos de mapeo
}
```

2. **POM con dependencias correctas:**
- `mapstruct` (dependencia)
- `mapstruct-processor` (en annotationProcessorPaths)
- `lombok` (si usas Lombok, también en annotationProcessorPaths)

3. **Compilación:**
- Ejecutar `mvn clean compile` para generar los Impl
- Los archivos se generan en `target/generated-sources/annotations/`

