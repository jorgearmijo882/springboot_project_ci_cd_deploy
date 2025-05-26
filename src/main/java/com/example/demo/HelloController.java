package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
  @GetMapping("/hello")
  public String hello() {
    return """
      <!DOCTYPE html>
      <html lang="es">
      <head>
        <meta charset="UTF-8">
        <title>Sobre Jorge Luis Armijo</title>
        <style>
          body { font-family: Arial, sans-serif; background: #f0f8ff; text-align: center; padding: 50px; }
          h1 { color: #2c3e50; }
          p { font-size: 1.2em; }
          a { color: #2980b9; text-decoration: none; }
          a:hover { text-decoration: underline; }
        </style>
      </head>
      <body>
        <h1>Jorge Luis Armijo Quito</h1>
        <p><strong>Teléfono:</strong> 0992554661</p>
        <p><strong>LinkedIn:</strong> <a href="https://www.linkedin.com/in/jorge-armijo-05051264/" target="_blank">Perfil de LinkedIn</a></p>
      </body>
      </html>
      """;
  }
}
